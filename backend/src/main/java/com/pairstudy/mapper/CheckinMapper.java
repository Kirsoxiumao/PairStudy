package com.pairstudy.mapper;
import com.pairstudy.entity.*;
import org.apache.ibatis.annotations.*;
import java.time.*;
import java.util.List;
public interface CheckinMapper {
 String FILTER="<where>group_id=#{group}<if test='date != null'> AND effective_date=#{date}</if><if test='category != null'> AND category_id=#{category}</if><if test='user != null'> AND user_id=#{user}</if></where>";
 @Select("<script>SELECT * FROM checkin "+FILTER+" ORDER BY created_at DESC,id DESC LIMIT #{size} OFFSET #{offset}</script>")
 List<Checkin> page(@Param("group") long group,@Param("date") LocalDate date,@Param("category") Long category,@Param("user") Long user,@Param("offset") int offset,@Param("size") int size);
 @Select("<script>SELECT COUNT(*) FROM checkin "+FILTER+"</script>")
 long count(@Param("group") long group,@Param("date") LocalDate date,@Param("category") Long category,@Param("user") Long user);
 @Select("SELECT * FROM checkin WHERE id=#{id} AND group_id=#{group}") Checkin get(@Param("id") long id,@Param("group") long group);
 @Select("SELECT * FROM checkin WHERE user_id=#{user} AND request_id=#{request}") Checkin byRequest(@Param("user") long user,@Param("request") String request);
 @Insert("INSERT INTO checkin(group_id,user_id,category_id,content,request_id,effective_date,created_at) VALUES(#{groupId},#{userId},#{categoryId},#{content},#{requestId},#{effectiveDate},#{createdAt})")
 @Options(useGeneratedKeys=true,keyProperty="id") int insert(Checkin c);
 @Insert("INSERT INTO checkin_image(checkin_id,upload_id,image_url,sort_order,created_at) VALUES(#{checkinId},#{uploadId},#{imageUrl},#{sortOrder},#{createdAt})") int image(CheckinImage image);
 @Select("SELECT * FROM checkin_image WHERE checkin_id=#{id} ORDER BY sort_order") List<CheckinImage> images(long id);
 @Delete("DELETE FROM checkin WHERE id=#{id} AND user_id=#{user} AND group_id=#{group}") int delete(@Param("id") long id,@Param("user") long user,@Param("group") long group);
 @Select("SELECT DISTINCT effective_date FROM checkin WHERE group_id=#{group} AND user_id=#{user} AND effective_date BETWEEN #{start} AND #{end} ORDER BY effective_date DESC")
 List<LocalDate> days(@Param("group") long group,@Param("user") long user,@Param("start") LocalDate start,@Param("end") LocalDate end);
 @Select("SELECT COUNT(*) FROM (SELECT effective_date FROM checkin WHERE group_id=#{group} AND effective_date BETWEEN #{start} AND #{end} GROUP BY effective_date HAVING COUNT(DISTINCT user_id)=2) d")
 long together(@Param("group") long group,@Param("start") LocalDate start,@Param("end") LocalDate end);
}
