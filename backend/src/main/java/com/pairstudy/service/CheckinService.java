package com.pairstudy.service;
import com.pairstudy.dto.Requests.CheckinInput;
import com.pairstudy.entity.*;
import com.pairstudy.exception.AppException;
import com.pairstudy.mapper.*;
import com.pairstudy.security.CurrentUser;
import com.pairstudy.util.StudyDayUtil;
import com.pairstudy.vo.Views.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
@Service
public class CheckinService {
 private final CheckinMapper mapper; private final CategoryMapper categories; private final UserMapper users; private final UploadMapper uploads;
 private final PairService pairs; private final CurrentUser current; private final StudyDayUtil time;
 public CheckinService(CheckinMapper mapper,CategoryMapper categories,UserMapper users,UploadMapper uploads,PairService pairs,CurrentUser current,StudyDayUtil time) { this.mapper=mapper; this.categories=categories; this.users=users; this.uploads=uploads; this.pairs=pairs; this.current=current; this.time=time; }
 @Transactional public CheckinView create(CheckinInput input) {
   var group=pairs.requireGroup(); long user=current.id(); users.lock(user); // also serializes retries for idempotency
   var existing=mapper.byRequest(user,input.requestId()); if(existing!=null) return view(existing,group);
   if(input.content().isBlank() && input.imageIds().isEmpty()) throw new AppException(400,"文字和图片不能同时为空");
   if(new HashSet<>(input.imageIds()).size()!=input.imageIds().size()) throw new AppException(400,"不能重复使用同一张图片");
   var category=categories.lock(input.categoryId(),group.id);
   if(category==null || category.archived) throw new AppException(400,"请选择有效的学习分区");
   List<UploadedImage> images=new ArrayList<>();
   for(long id:input.imageIds()) { var u=uploads.lock(id,user,group.id); if(u==null || u.claimed) throw new AppException(400,"图片不可用，请重新上传"); images.add(u); }
   var instant=time.instant(); var c=new Checkin(); c.userId=user; c.groupId=group.id; c.categoryId=category.id;
   c.content=input.content().trim(); c.requestId=input.requestId(); c.createdAt=time.localTime(instant); c.effectiveDate=time.effectiveDate(instant); mapper.insert(c);
   for(int i=0;i<images.size();i++) {
     var u=images.get(i); uploads.claim(u.id); var image=new CheckinImage(); image.checkinId=c.id; image.uploadId=u.id; image.imageUrl=u.imageUrl; image.sortOrder=i; image.createdAt=c.createdAt; mapper.image(image);
   }
   return view(c,group);
 }
 public CheckinView get(long id) { var group=pairs.requireGroup(); var c=mapper.get(id,group.id); if(c==null) throw new AppException(404,"记录不存在"); return view(c,group); }
 public Page<CheckinView> page(LocalDate date,Long category,String author,int page,int size) {
   if(page<1 || page>100000 || size<1 || size>50) throw new AppException(400,"分页参数不正确");
   var group=pairs.requireGroup(); long me=current.id();
   if(category!=null && categories.get(category,group.id)==null) throw new AppException(404,"分区不存在");
   Long user=switch(author) { case "all" -> null; case "self" -> me; case "partner" -> group.userAId==me?group.userBId:group.userAId; default -> throw new AppException(400,"筛选条件不正确"); };
   long total=mapper.count(group.id,date,category,user);
   var items=mapper.page(group.id,date,category,user,(page-1)*size,size).stream().map(c->view(c,group)).toList();
   return new Page<>(items,page,size,total,(long)page*size<total);
 }
 @Transactional public void delete(long id) {
   var group=pairs.requireGroup(); var c=mapper.get(id,group.id); if(c==null) throw new AppException(404,"记录不存在");
   if(!c.userId.equals(current.id())) throw new AppException(403,"只能删除自己的打卡"); mapper.delete(id,current.id(),group.id);
 }
 private CheckinView view(Checkin c,PairGroup group) {
   var u=users.get(c.userId); var category=categories.get(c.categoryId,group.id);
   return new CheckinView(c.id,c.userId,u.nickname,u.avatar,c.userId.equals(group.userAId)?"A":"B",c.categoryId,category.name,c.content,c.effectiveDate,c.createdAt,mapper.images(c.id));
 }
}
