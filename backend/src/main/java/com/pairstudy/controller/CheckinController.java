package com.pairstudy.controller;
import com.pairstudy.dto.Requests.CheckinInput;
import com.pairstudy.service.CheckinService;
import com.pairstudy.vo.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/checkins")
public class CheckinController {
 private final CheckinService service;
 public CheckinController(CheckinService service) { this.service=service; }
 @PostMapping public ApiResponse<Views.CheckinView> create(@Valid @RequestBody CheckinInput input) { return ApiResponse.ok(service.create(input)); }
 @GetMapping("/feed") public ApiResponse<Views.Page<Views.CheckinView>> feed(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size) { return ApiResponse.ok(service.page(null,null,"all",page,size)); }
 @GetMapping("/{id}") public ApiResponse<Views.CheckinView> get(@PathVariable long id) { return ApiResponse.ok(service.get(id)); }
 @GetMapping("/date/{date}") public ApiResponse<Views.Page<Views.CheckinView>> date(@PathVariable LocalDate date,@RequestParam(defaultValue="all") String author,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size) { return ApiResponse.ok(service.page(date,null,author,page,size)); }
 @GetMapping("/category/{id}") public ApiResponse<Views.Page<Views.CheckinView>> category(@PathVariable long id,@RequestParam(defaultValue="all") String author,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size) { return ApiResponse.ok(service.page(null,id,author,page,size)); }
 @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable long id) { service.delete(id); return ApiResponse.ok(null); }
}
