package com.pairstudy.vo;
import com.pairstudy.entity.*;
import java.time.*;
import java.util.List;
public final class Views {
 public record UserView(Long id,String username,String nickname,String avatar,Long groupId) {
   public static UserView of(User u) { return new UserView(u.id,u.username,u.nickname,u.avatar,u.groupId); }
 }
 public record Auth(String token,UserView user) {}
 public record Pair(Long groupId,UserView userA,UserView userB,String inviteCode,LocalDateTime createdAt,LocalDateTime boundAt,
    LocalDate effectiveDate,Instant serverNow,Instant nextResetAt) {}
 public record CheckinView(Long id,Long userId,String nickname,String avatar,String role,Long categoryId,String categoryName,
   String content,LocalDate effectiveDate,LocalDateTime createdAt,List<CheckinImage> images) {}
 public record Page<T>(List<T> items,int page,int size,long total,boolean hasMore) {}
 public record Day(LocalDate date,boolean selfChecked,boolean partnerChecked) {}
 public record Profile(UserView user,UserView partner,LocalDateTime boundAt) {}
 public record Statistics(long totalCheckins,long monthDays,long currentStreak,long togetherDays,LocalDate effectiveDate) {}
 public record Upload(Long id,String imageUrl) {}
}
