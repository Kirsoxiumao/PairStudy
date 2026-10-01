package com.pairstudy.service;
import com.pairstudy.mapper.*;
import com.pairstudy.security.CurrentUser;
import com.pairstudy.util.StudyDayUtil;
import com.pairstudy.vo.Views.*;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;
@Service
public class StatisticsService {
 private final CheckinMapper mapper; private final PairService pairs; private final CurrentUser current; private final UserMapper users; private final StudyDayUtil time;
 public StatisticsService(CheckinMapper mapper,PairService pairs,CurrentUser current,UserMapper users,StudyDayUtil time) { this.mapper=mapper; this.pairs=pairs; this.current=current; this.users=users; this.time=time; }
 public List<Day> month(int year,int month) {
   if(year<1970 || year>9999) throw new IllegalArgumentException();
   var range=YearMonth.of(year,month); var group=pairs.requireGroup(); long me=current.id(); long partner=group.userAId==me?group.userBId:group.userAId;
   var self=new HashSet<>(mapper.days(group.id,me,range.atDay(1),range.atEndOfMonth()));
   var friend=new HashSet<>(mapper.days(group.id,partner,range.atDay(1),range.atEndOfMonth()));
   return range.atDay(1).datesUntil(range.atEndOfMonth().plusDays(1)).map(d->new Day(d,self.contains(d),friend.contains(d))).toList();
 }
 public Profile profile() { var g=pairs.requireGroup(); var me=current.user(); long partner=g.userAId.equals(me.id)?g.userBId:g.userAId; return new Profile(UserView.of(me),UserView.of(users.get(partner)),g.boundAt); }
 public Statistics statistics() {
   var group=pairs.requireGroup(); long me=current.id(); var today=time.today();
   // Include binding's learning date, including a binding made before 04:00.
   var first=time.effectiveDate(group.boundAt);
   var days=new HashSet<>(mapper.days(group.id,me,first,today));
   long monthDays=days.stream().filter(d->YearMonth.from(d).equals(YearMonth.from(today))).count();
   return new Statistics(mapper.count(group.id,null,null,me),monthDays,streak(days,today),mapper.together(group.id,first,today),today);
 }
 public static long streak(Set<LocalDate> days,LocalDate today) {
   // Before today's deadline the yesterday-ending streak is still current.
   LocalDate cursor=days.contains(today)?today:today.minusDays(1); long n=0;
   while(days.contains(cursor)) { n++; cursor=cursor.minusDays(1); } return n;
 }
}
