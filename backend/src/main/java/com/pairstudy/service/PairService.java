package com.pairstudy.service;
import com.pairstudy.entity.*;
import com.pairstudy.exception.AppException;
import com.pairstudy.mapper.*;
import com.pairstudy.security.CurrentUser;
import com.pairstudy.util.StudyDayUtil;
import com.pairstudy.vo.Views.*;
import java.security.SecureRandom;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class PairService {
 private final PairMapper pairs; private final UserMapper users; private final CurrentUser current; private final StudyDayUtil time;
 public PairService(PairMapper pairs,UserMapper users,CurrentUser current,StudyDayUtil time) { this.pairs=pairs; this.users=users; this.current=current; this.time=time; }
 public PairGroup requireGroup() {
   var user=current.user(); var group=user.groupId==null?null:pairs.get(user.groupId);
   if(group==null || group.userBId==null) throw new AppException(409,"请先与朋友完成绑定");
   if(!user.id.equals(group.userAId) && !user.id.equals(group.userBId)) throw new AppException(403,"无权访问该空间");
   return group;
 }
 public Pair info() { var u=current.user(); return view(u.groupId==null?null:pairs.get(u.groupId)); }
 @Transactional public Pair createInvite() {
   pairs.membershipLock(); var user=users.lock(current.id());
   if(user.groupId!=null) return view(pairs.get(user.groupId));
   byte[] bytes=new byte[8]; new SecureRandom().nextBytes(bytes);
   var g=new PairGroup(); g.userAId=user.id; g.inviteCode=HexFormat.of().formatHex(bytes).toUpperCase(); g.createdAt=time.now();
   pairs.insert(g); users.setGroup(user.id,g.id,time.now()); return view(g);
 }
 @Transactional public Pair bind(String code) {
   pairs.membershipLock(); var me=users.lock(current.id()); var target=pairs.byCode(code.toUpperCase());
   if(target==null) throw new AppException(404,"邀请码不存在");
   if(target.userAId.equals(me.id)) throw new AppException(400,"不能绑定自己的邀请码");
   if(target.userBId!=null) throw new AppException(409,"这个空间已经有两人了");
   if(me.groupId!=null) {
     var old=pairs.get(me.groupId);
     if(old.userBId!=null) throw new AppException(409,"你已经绑定搭子");
     // Pending groups cannot create any category, upload or checkin.
     pairs.deleteEmpty(old.id);
   }
   if(pairs.bind(target.id,me.id,time.now())!=1) throw new AppException(409,"邀请码已使用");
   users.setGroup(me.id,target.id,time.now()); return view(pairs.get(target.id));
 }
 private Pair view(PairGroup g) {
   return new Pair(g==null?null:g.id,g==null?null:UserView.of(users.get(g.userAId)),
     g==null || g.userBId==null?null:UserView.of(users.get(g.userBId)),g==null?null:g.inviteCode,
     g==null?null:g.createdAt,g==null?null:g.boundAt,time.today(),time.instant(),time.nextReset());
 }
}
