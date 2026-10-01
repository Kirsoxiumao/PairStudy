package com.pairstudy.security;
import com.pairstudy.entity.User;
import com.pairstudy.exception.AppException;
import com.pairstudy.mapper.UserMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
@Component
public class CurrentUser {
 private final UserMapper users;
 public CurrentUser(UserMapper users) { this.users=users; }
 public long id() {
   var auth=SecurityContextHolder.getContext().getAuthentication();
   if(auth==null || !(auth.getPrincipal() instanceof Jwt jwt)) throw new AppException(401,"请先登录");
   try { return Long.parseLong(jwt.getSubject()); } catch(Exception e) { throw new AppException(401,"登录已失效"); }
 }
 public User user() { var u=users.get(id()); if(u==null) throw new AppException(401,"账号不存在"); return u; }
}
