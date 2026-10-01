package com.pairstudy.controller;
import com.pairstudy.dto.Requests.Bind;
import com.pairstudy.service.PairService;
import com.pairstudy.vo.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/pair")
public class PairController {
 private final PairService service;
 public PairController(PairService service) { this.service=service; }
 @PostMapping("/create-invite") public ApiResponse<Views.Pair> invite() { return ApiResponse.ok(service.createInvite()); }
 @PostMapping("/bind") public ApiResponse<Views.Pair> bind(@Valid @RequestBody Bind input) { return ApiResponse.ok(service.bind(input.inviteCode())); }
 @GetMapping("/info") public ApiResponse<Views.Pair> info() { return ApiResponse.ok(service.info()); }
}
