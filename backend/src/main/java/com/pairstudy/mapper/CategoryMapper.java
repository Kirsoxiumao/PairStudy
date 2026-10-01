package com.pairstudy.mapper;
import com.pairstudy.entity.Category;
import org.apache.ibatis.annotations.*;
import java.util.List;
public interface CategoryMapper {
 @Select("SELECT * FROM category WHERE group_id=#{group} ORDER BY archived,sort_order,id") List<Category> list(long group);
 @Select("SELECT * FROM category WHERE id=#{id} AND group_id=#{group}") Category get(@Param("id") long id,@Param("group") long group);
 @Select("SELECT * FROM category WHERE id=#{id} AND group_id=#{group} FOR UPDATE") Category lock(@Param("id") long id,@Param("group") long group);
 @Insert("INSERT INTO category(group_id,name,icon_name,sort_order,created_at,updated_at) VALUES(#{groupId},#{name},#{iconName},#{sortOrder},#{createdAt},#{updatedAt})")
 @Options(useGeneratedKeys=true,keyProperty="id") int insert(Category c);
 @Update("UPDATE category SET name=#{name},icon_name=#{iconName},sort_order=#{sortOrder},updated_at=#{updatedAt} WHERE id=#{id} AND group_id=#{groupId} AND archived=FALSE") int update(Category c);
 @Update("UPDATE category SET archived=TRUE,updated_at=#{now} WHERE id=#{id} AND group_id=#{group}")
 int archive(@Param("id") long id,@Param("group") long group,@Param("now") java.time.LocalDateTime now);
}
