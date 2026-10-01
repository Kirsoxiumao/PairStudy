package com.pairstudy.service;
import com.pairstudy.dto.Requests.*;
import com.pairstudy.entity.User;
import com.pairstudy.exception.AppException;
import com.pairstudy.mapper.UserMapper;
import com.pairstudy.util.StudyDayUtil;
import com.pairstudy.vo.Views.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import java.time.temporal.ChronoUnit;
import java.nio.charset.StandardCharsets;
@Service
public class AuthService {
 private final UserMapper users; private final PasswordEncoder passwords; private final JwtEncoder jwt; private final StudyDayUtil time; private final long hours;
 public AuthService(UserMapper users,PasswordEncoder passwords,JwtEncoder jwt,StudyDayUtil time,@Value("${app.jwt-hours}") long hours) { this.users=users; this.passwords=passwords; this.jwt=jwt; this.time=time; this.hours=hours; }
 public Auth register(Register input) {
   if(input.password().getBytes(StandardCharsets.UTF_8).length>72) throw new AppException(400,"密码 UTF-8 长度不能超过 72 字节");
   if(users.byName(input.username())!=null) throw new AppException(409,"用户名已存在");
   User u=new User(); u.username=input.username(); u.password=passwords.encode(input.password()); u.nickname=input.nickname().trim(); u.avatar=""; u.createdAt=time.now(); u.updatedAt=u.createdAt; users.insert(u); return token(u);
 }
 public Auth login(Login input) {
   User u=users.byName(input.username());
   if(u==null || !passwords.matches(input.password(),u.password)) throw new AppException(401,"用户名或密码错误");
   return token(u);
 }
 private Auth token(User u) {
   var now=time.instant(); var claims=JwtClaimsSet.builder().issuer("pairstudy").subject(u.id.toString()).issuedAt(now).expiresAt(now.plus(hours,ChronoUnit.HOURS)).build();
   String token=jwt.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
   return new Auth(token,UserView.of(u));
 }
}
