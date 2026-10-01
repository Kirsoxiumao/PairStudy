package com.pairstudy;
import com.pairstudy.util.StudyDayUtil;
import com.pairstudy.service.StatisticsService;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
class StudyDayTest {
 private final StudyDayUtil time=new StudyDayUtil(Clock.systemUTC(),"Asia/Shanghai");
 @Test void boundaries() {
   assertEquals(LocalDate.of(2026,10,1),time.effectiveDate(LocalDateTime.parse("2026-10-02T00:35:00")));
   assertEquals(LocalDate.of(2026,10,1),time.effectiveDate(LocalDateTime.parse("2026-10-02T03:59:59.999999")));
   assertEquals(LocalDate.of(2026,10,2),time.effectiveDate(LocalDateTime.parse("2026-10-02T04:00:00")));
   assertEquals(LocalDate.of(2026,10,2),time.effectiveDate(LocalDateTime.parse("2026-10-02T23:50:00")));
 }
 @Test void monthAndYearRollover() {
   assertEquals(LocalDate.of(2025,12,31),time.effectiveDate(LocalDateTime.parse("2026-01-01T03:59:59")));
   assertEquals(LocalDate.of(2024,2,29),time.effectiveDate(LocalDateTime.parse("2024-03-01T00:00:00")));
 }
 @Test void serverZoneIndependentOfMachineZone() {
   assertEquals(LocalDate.of(2026,10,1),time.effectiveDate(Instant.parse("2026-10-01T19:59:59Z")));
   assertEquals(LocalDate.of(2026,10,2),time.effectiveDate(Instant.parse("2026-10-01T20:00:00Z")));
 }
 @Test void streakDoesNotResetUntilDeadline() {
   LocalDate today=LocalDate.of(2026,10,2);
   assertEquals(2,StatisticsService.streak(Set.of(today.minusDays(1),today.minusDays(2)),today));
   assertEquals(3,StatisticsService.streak(Set.of(today,today.minusDays(1),today.minusDays(2)),today));
   assertEquals(0,StatisticsService.streak(Set.of(today.minusDays(2)),today));
 }
}
