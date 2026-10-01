package com.pairstudy.controller;
import com.pairstudy.dto.Requests.*;
import com.pairstudy.service.AuthService;
import com.pairstudy.vo.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final AuthService service;
 public AuthController(AuthService service) { this.service=service; }
 @PostMapping("/register") public ApiResponse<Views.Auth> register(@Valid @RequestBody Register input) { return ApiResponse.ok(service.register(input)); }
 @PostMapping("/login") public ApiResponse<Views.Auth> login(@Valid @RequestBody Login input) { return ApiResponse.ok(service.login(input)); }
}
