package com.pairstudy.controller;
import com.pairstudy.service.UploadService;
import com.pairstudy.vo.*;
import org.springframework.http.*;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
@RestController
public class UploadController {
 private final UploadService service;
 public UploadController(UploadService service) { this.service=service; }
 @PostMapping("/api/upload/image") public ApiResponse<Views.Upload> upload(@RequestParam("file") MultipartFile file) throws IOException { return ApiResponse.ok(service.upload(file)); }
 @GetMapping("/uploads/checkin/{name}") public ResponseEntity<Resource> image(@PathVariable String name) {
   var u=service.accessible(name);
   return ResponseEntity.ok().contentType(MediaType.parseMediaType(u.mediaType)).cacheControl(CacheControl.noStore())
     .header("X-Content-Type-Options","nosniff").body(service.resource(u));
 }
}
