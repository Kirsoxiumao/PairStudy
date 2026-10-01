package com.pairstudy.util;
import java.time.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class StudyDayUtil {
    private final Clock clock;
    private final ZoneId zone;
    public StudyDayUtil(Clock clock, @Value("${app.zone}") String zone) { this.clock=clock; this.zone=ZoneId.of(zone); }
    public LocalDate effectiveDate(Instant instant) { return effectiveDate(LocalDateTime.ofInstant(instant,zone)); }
    public LocalDate effectiveDate(LocalDateTime localTime) { return localTime.minusHours(4).toLocalDate(); }
    public LocalDate today() { return effectiveDate(clock.instant()); }
    public LocalDateTime now() { return LocalDateTime.ofInstant(clock.instant(), zone); }
    public Instant instant() { return clock.instant(); }
    public LocalDateTime localTime(Instant instant) { return LocalDateTime.ofInstant(instant, zone); }
    public Instant nextReset() { return today().plusDays(1).atTime(4,0).atZone(zone).toInstant(); }
}
