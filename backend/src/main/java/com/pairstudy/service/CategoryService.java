package com.pairstudy.service;
import com.pairstudy.entity.Category;
import com.pairstudy.dto.Requests.CategoryInput;
import com.pairstudy.exception.AppException;
import com.pairstudy.mapper.CategoryMapper;
import com.pairstudy.util.StudyDayUtil;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class CategoryService {
 private final CategoryMapper mapper; private final PairService pairs; private final StudyDayUtil time;
 public CategoryService(CategoryMapper mapper,PairService pairs,StudyDayUtil time) { this.mapper=mapper; this.pairs=pairs; this.time=time; }
 public List<Category> list() { return mapper.list(pairs.requireGroup().id); }
 public Category get(long id,long group) { var c=mapper.get(id,group); if(c==null) throw new AppException(404,"分区不存在"); return c; }
 @Transactional public Category create(CategoryInput input) { var c=new Category(); c.groupId=pairs.requireGroup().id; fill(c,input); c.createdAt=time.now(); mapper.insert(c); return c; }
 @Transactional public Category update(long id,CategoryInput input) {
   var c=mapper.lock(id,pairs.requireGroup().id); if(c==null) throw new AppException(404,"分区不存在");
   if(c.archived) throw new AppException(409,"已归档分区不能修改"); fill(c,input); mapper.update(c); return c;
 }
 @Transactional public void archive(long id) { long group=pairs.requireGroup().id; get(id,group); mapper.archive(id,group,time.now()); }
 private void fill(Category c,CategoryInput input) { c.name=input.name().trim(); c.iconName=input.iconName(); c.sortOrder=input.sortOrder(); c.updatedAt=time.now(); }
}
