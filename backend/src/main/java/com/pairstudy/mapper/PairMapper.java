package com.pairstudy.mapper;
import com.pairstudy.entity.PairGroup;
import org.apache.ibatis.annotations.*;
public interface PairMapper {
 @Select("SELECT id FROM pair_lock WHERE id=1 FOR UPDATE") int membershipLock();
 @Select("SELECT * FROM pair_group WHERE id=#{id}") PairGroup get(long id);
 @Select("SELECT * FROM pair_group WHERE id=#{id} FOR UPDATE") PairGroup lock(long id);
 @Select("SELECT * FROM pair_group WHERE invite_code=#{code}") PairGroup byCode(String code);
 @Insert("INSERT INTO pair_group(user_a_id,invite_code,created_at) VALUES(#{userAId},#{inviteCode},#{createdAt})")
 @Options(useGeneratedKeys=true,keyProperty="id") int insert(PairGroup pair);
 @Update("UPDATE pair_group SET user_b_id=#{user},bound_at=#{now} WHERE id=#{id} AND user_b_id IS NULL")
 int bind(@Param("id") long id,@Param("user") long user,@Param("now") java.time.LocalDateTime now);
 @Delete("DELETE FROM pair_group WHERE id=#{id} AND user_b_id IS NULL") int deleteEmpty(long id);
}
