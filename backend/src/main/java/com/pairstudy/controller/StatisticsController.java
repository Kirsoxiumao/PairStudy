package com.pairstudy.controller;
import com.pairstudy.service.StatisticsService;
import com.pairstudy.vo.*;
import java.util.List;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api")
public class StatisticsController {
 private final StatisticsService service;
 public StatisticsController(StatisticsService service) { this.service=service; }
 @GetMapping("/calendar/month") public ApiResponse<List<Views.Day>> month(@RequestParam int year,@RequestParam int month) { return ApiResponse.ok(service.month(year,month)); }
 @GetMapping("/profile") public ApiResponse<Views.Profile> profile() { return ApiResponse.ok(service.profile()); }
 @GetMapping("/profile/statistics") public ApiResponse<Views.Statistics> stats() { return ApiResponse.ok(service.statistics()); }
 @GetMapping("/health") public ApiResponse<String> health() { return ApiResponse.ok("PairStudy"); }
}
