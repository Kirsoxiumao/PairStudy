package com.pairstudy.controller;
import com.pairstudy.dto.Requests.CategoryInput;
import com.pairstudy.entity.Category;
import com.pairstudy.service.CategoryService;
import com.pairstudy.vo.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/categories")
public class CategoryController {
 private final CategoryService service;
 public CategoryController(CategoryService service) { this.service=service; }
 @GetMapping public ApiResponse<List<Category>> list() { return ApiResponse.ok(service.list()); }
 @PostMapping public ApiResponse<Category> create(@Valid @RequestBody CategoryInput input) { return ApiResponse.ok(service.create(input)); }
 @PutMapping("/{id}") public ApiResponse<Category> update(@PathVariable long id,@Valid @RequestBody CategoryInput input) { return ApiResponse.ok(service.update(id,input)); }
 @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable long id) { service.archive(id); return ApiResponse.ok(null); }
}
