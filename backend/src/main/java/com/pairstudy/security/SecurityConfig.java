package com.pairstudy.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pairstudy.vo.ApiResponse;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
public class SecurityConfig {
 @Bean SecretKeySpec key(@Value("${app.jwt-secret}") String secret) {
   if(secret.getBytes(StandardCharsets.UTF_8).length<32 || secret.startsWith("REPLACE_")) throw new IllegalStateException("Set JWT_SECRET to a random secret of at least 32 bytes");
   return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256");
 }
 @Bean JwtEncoder encoder(SecretKeySpec key) { return new NimbusJwtEncoder(new ImmutableSecret<>(key)); }
 @Bean JwtDecoder decoder(SecretKeySpec key) {
   var d=NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
   d.setJwtValidator(JwtValidators.createDefaultWithIssuer("pairstudy")); return d;
 }
 @Bean PasswordEncoder passwords() { return new BCryptPasswordEncoder(12); }
 @Bean SecurityFilterChain chain(HttpSecurity http,ObjectMapper json) throws Exception {
   return http.csrf(c->c.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
     .authorizeHttpRequests(a->a.requestMatchers("/api/auth/login","/api/auth/register","/api/health").permitAll().anyRequest().authenticated())
     .oauth2ResourceServer(o->o.jwt(j->{}).authenticationEntryPoint((req,res,e)->{
       res.setStatus(401); res.setContentType("application/json;charset=UTF-8"); json.writeValue(res.getOutputStream(),new ApiResponse<>(401,"登录已过期，请重新登录",null));
     }))
     .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->{
       res.setStatus(401); res.setContentType("application/json;charset=UTF-8"); json.writeValue(res.getOutputStream(),new ApiResponse<>(401,"请先登录",null));
     }).accessDeniedHandler((req,res,ex)->{
       res.setStatus(403); res.setContentType("application/json;charset=UTF-8"); json.writeValue(res.getOutputStream(),new ApiResponse<>(403,"无权访问",null));
     })).build();
 }
}
