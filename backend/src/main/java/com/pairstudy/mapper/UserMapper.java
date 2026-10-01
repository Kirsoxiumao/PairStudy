package com.pairstudy.mapper;
import com.pairstudy.entity.User;
import org.apache.ibatis.annotations.*;
public interface UserMapper {
 @Select("SELECT * FROM `user` WHERE id=#{id}") User get(long id);
 @Select("SELECT * FROM `user` WHERE id=#{id} FOR UPDATE") User lock(long id);
 @Select("SELECT * FROM `user` WHERE username=#{name}") User byName(String name);
 @Insert("INSERT INTO `user`(username,password,nickname,avatar,created_at,updated_at) VALUES(#{username},#{password},#{nickname},#{avatar},#{createdAt},#{updatedAt})")
 @Options(useGeneratedKeys=true,keyProperty="id") int insert(User user);
 @Update("UPDATE `user` SET group_id=#{group},updated_at=#{now} WHERE id=#{id}") int setGroup(@Param("id") long id,@Param("group") long group,@Param("now") java.time.LocalDateTime now);
}
