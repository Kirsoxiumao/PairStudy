package com.pairstudy;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
@SpringBootTest(properties={"app.jwt-secret=integration-test-only-secret-32-bytes-long","app.upload-dir=target/test-uploads"})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(ApiIntegrationTest.TimeConfiguration.class)
class ApiIntegrationTest {
 @Autowired MockMvc mvc; @Autowired ObjectMapper json; @Autowired JdbcTemplate jdbc; @Autowired TestClock clock;
 @DynamicPropertySource static void db(DynamicPropertyRegistry r) {
   r.add("spring.datasource.url",()->System.getProperty("test.db.url","jdbc:h2:mem:pairstudy;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"));
   r.add("spring.datasource.username",()->System.getProperty("test.db.user","sa"));
   r.add("spring.datasource.password",()->System.getProperty("test.db.password",""));
 }
 @TestConfiguration static class TimeConfiguration { @Bean @Primary TestClock testClock() { return new TestClock(); } }
 static class TestClock extends Clock {
   final AtomicReference<Instant> time=new AtomicReference<>(Instant.now());
   void at(String local) { time.set(LocalDateTime.parse(local).atZone(ZoneId.of("Asia/Shanghai")).toInstant()); }
   public ZoneId getZone() { return ZoneOffset.UTC; } public Clock withZone(ZoneId zone) { return this; } public Instant instant() { return time.get(); }
 }
 @BeforeAll void schema() throws Exception {
   String sql=Files.readString(Path.of("../database/schema.sql"));
   sql=sql.replaceAll("(?m)^--.*$","").replaceAll("CREATE DATABASE[^;]*;","").replaceAll("USE pairstudy;","");
   for(String statement:sql.split(";")) if(!statement.isBlank()) jdbc.execute(statement);
 }
 @BeforeEach void now() { clock.time.set(Instant.now()); }
 record Account(String token,long id) {}
 Account account() throws Exception {
   String username="u"+UUID.randomUUID().toString().replace("-","").substring(0,20);
   JsonNode auth=request("POST","/api/auth/register",null,Map.of("username",username,"password","Study123456","nickname",username),200);
   assertFalse(jdbc.queryForObject("SELECT password FROM `user` WHERE id=?",String.class,auth.path("user").path("id").asLong()).equals("Study123456"));
   request("POST","/api/auth/login",null,Map.of("username",username,"password","Study123456"),200);
   request("POST","/api/auth/login",null,Map.of("username",username,"password","incorrect"),401);
   return new Account(auth.path("token").asText(),auth.path("user").path("id").asLong());
 }
 JsonNode request(String method,String path,String token,Object body,int status) throws Exception {
   var b=org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(method),path);
   if(token!=null) b.header("Authorization","Bearer "+token);
   if(body!=null) b.contentType("application/json").content(json.writeValueAsBytes(body));
   var response=mvc.perform(b).andReturn().getResponse();
   assertEquals(status,response.getStatus(),response.getContentAsString());
   return json.readTree(response.getContentAsString()).path("data");
 }
 String invite(Account a) throws Exception { return request("POST","/api/pair/create-invite",a.token,null,200).path("inviteCode").asText(); }
 void bind(Account a,Account b) throws Exception { request("POST","/api/pair/bind",b.token,Map.of("inviteCode",invite(a)),200); }
 long category(Account a) throws Exception { return request("POST","/api/categories",a.token,Map.of("name","阅读","iconName","book","sortOrder",0),200).path("id").asLong(); }
 Map<String,Object> post(long category,List<Long> images,String content) { return Map.of("categoryId",category,"imageIds",images,"content",content,"requestId",UUID.randomUUID().toString()); }
 @Test void completeWorkflowAndAccessControl() throws Exception {
   Account a=account(),b=account(),c=account(),d=account(); bind(a,b); bind(c,d);
   long category=category(a); long foreignCategory=category(c);
   request("GET","/api/calendar/month?year=2026",a.token,null,400);
   request("GET","/api/checkins/feed?page=0",a.token,null,400);
   assertEquals(400,mvc.perform(multipart("/api/upload/image").header("Authorization","Bearer "+a.token)).andReturn().getResponse().getStatus());
   assertEquals(1,request("GET","/api/categories",b.token,null,200).size());
   request("GET","/api/categories",null,null,401);
   request("GET","/api/categories","invalid.jwt",null,401);
   request("POST","/api/checkins",a.token,post(foreignCategory,List.of(),"跨组"),400);
   request("POST","/api/checkins",a.token,post(category,List.of(),"  "),400);
   var file=new MockMultipartFile("file","pixel.png","image/png",Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Wl2b3sAAAAASUVORK5CYII="));
   var upload=mvc.perform(multipart("/api/upload/image").file(file).header("Authorization","Bearer "+a.token)).andReturn().getResponse();
   assertEquals(200,upload.getStatus(),upload.getContentAsString()); var uploaded=json.readTree(upload.getContentAsString()).path("data"); long image=uploaded.path("id").asLong(); String url=uploaded.path("imageUrl").asText();
   request("POST","/api/checkins",c.token,post(foreignCategory,List.of(image),"偷图"),400);
   request("POST","/api/checkins",b.token,post(category,List.of(image),"非上传者"),400);
   var input=post(category,List.of(image),"今天读了三章");
   var record=request("POST","/api/checkins",a.token,input,200); long id=record.path("id").asLong();
   assertEquals(id,request("POST","/api/checkins",a.token,input,200).path("id").asLong());
   assertEquals(1,request("GET","/api/checkins/feed",b.token,null,200).path("items").size());
   request("GET","/api/checkins/"+id,c.token,null,404);
   request("DELETE","/api/checkins/"+id,b.token,null,403);
   request("GET","/api/checkins/category/"+category,c.token,null,404);
   assertEquals(200,mvc.perform(get(url).header("Authorization","Bearer "+b.token)).andReturn().getResponse().getStatus());
   assertEquals(404,mvc.perform(get(url).header("Authorization","Bearer "+c.token)).andReturn().getResponse().getStatus());
   assertEquals(401,mvc.perform(get(url)).andReturn().getResponse().getStatus());
   var bad=new MockMultipartFile("file","bad.jpg","image/jpeg","executable script".getBytes());
   assertEquals(400,mvc.perform(multipart("/api/upload/image").file(bad).header("Authorization","Bearer "+a.token)).andReturn().getResponse().getStatus());
   request("POST","/api/checkins",b.token,post(category,List.of(),"搭子也完成了"),200);
   var stats=request("GET","/api/profile/statistics",a.token,null,200);
   assertEquals(1,stats.path("togetherDays").asInt()); assertEquals(1,stats.path("monthDays").asInt());
   String day=record.path("effectiveDate").asText(); var date=LocalDate.parse(day);
   var month=request("GET","/api/calendar/month?year="+date.getYear()+"&month="+date.getMonthValue(),b.token,null,200);
   assertTrue(month.get(date.getDayOfMonth()-1).path("selfChecked").asBoolean()); assertTrue(month.get(date.getDayOfMonth()-1).path("partnerChecked").asBoolean());
   assertEquals(2,request("GET","/api/checkins/date/"+day,a.token,null,200).path("total").asInt());
   assertTrue(request("GET","/api/checkins/feed?page=1&size=1",b.token,null,200).path("hasMore").asBoolean());
   request("DELETE","/api/categories/"+category,a.token,null,200);
   assertTrue(request("GET","/api/categories",b.token,null,200).get(0).path("archived").asBoolean());
   request("POST","/api/checkins",a.token,post(category,List.of(),"归档后"),400);
   request("GET","/api/checkins/"+id,b.token,null,200);
   request("DELETE","/api/checkins/"+id,a.token,null,200);
   assertEquals(0,request("GET","/api/profile/statistics",a.token,null,200).path("togetherDays").asInt());
 }
 @Test void fourAmIsEnforcedByApi() throws Exception {
   Account a=account(),b=account(); bind(a,b); long c=category(a);
   // Tokens issued at real current time remain valid; only business clock is controlled.
   clock.at("2026-10-02T03:59:59");
   var before=request("POST","/api/checkins",a.token,post(c,List.of(),"边界前"),200);
   clock.at("2026-10-02T04:00:00");
   var after=request("POST","/api/checkins",a.token,post(c,List.of(),"边界后"),200);
   assertEquals("2026-10-01",before.path("effectiveDate").asText()); assertEquals("2026-10-02",after.path("effectiveDate").asText());
   assertTrue(before.path("createdAt").asText().startsWith("2026-10-02T03:59:59"));
   assertEquals(1,request("GET","/api/checkins/date/2026-10-01",a.token,null,200).path("total").asInt());
 }
 @Test void onlyOneConcurrentPartnerCanBind() throws Exception {
   Account a=account(),b=account(),c=account(); String code=invite(a);
   // Both invitees may already have their own pending invitations.
   invite(b); invite(c);
   ExecutorService pool=Executors.newFixedThreadPool(2); CountDownLatch gate=new CountDownLatch(1);
   try {
     List<Future<Integer>> results=new ArrayList<>();
     for(Account user:List.of(b,c)) results.add(pool.submit(()->{ gate.await(); return mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/pair/bind").header("Authorization","Bearer "+user.token).contentType("application/json").content(json.writeValueAsBytes(Map.of("inviteCode",code)))).andReturn().getResponse().getStatus(); }));
     gate.countDown(); var statuses=new ArrayList<Integer>(); for(var f:results) statuses.add(f.get(15,TimeUnit.SECONDS)); Collections.sort(statuses);
     assertEquals(List.of(200,409),statuses);
   } finally { pool.shutdownNow(); }
   request("POST","/api/pair/bind",a.token,Map.of("inviteCode",code),400);
 }
}
