package com.pairstudy.mapper;
import com.pairstudy.entity.UploadedImage;
import org.apache.ibatis.annotations.*;
public interface UploadMapper {
 @Insert("INSERT INTO uploaded_image(user_id,group_id,filename,image_url,media_type,created_at) VALUES(#{userId},#{groupId},#{filename},#{imageUrl},#{mediaType},#{createdAt})")
 @Options(useGeneratedKeys=true,keyProperty="id") int insert(UploadedImage u);
 @Select("SELECT * FROM uploaded_image WHERE id=#{id} AND user_id=#{user} AND group_id=#{group} FOR UPDATE")
 UploadedImage lock(@Param("id") long id,@Param("user") long user,@Param("group") long group);
 @Update("UPDATE uploaded_image SET claimed=TRUE WHERE id=#{id} AND claimed=FALSE") int claim(long id);
 @Select("SELECT * FROM uploaded_image WHERE filename=#{name} AND group_id=#{group} AND (user_id=#{user} OR EXISTS(SELECT 1 FROM checkin_image i JOIN checkin c ON c.id=i.checkin_id WHERE i.upload_id=uploaded_image.id AND c.group_id=#{group}))")
 UploadedImage accessible(@Param("name") String name,@Param("group") long group,@Param("user") long user);
}
