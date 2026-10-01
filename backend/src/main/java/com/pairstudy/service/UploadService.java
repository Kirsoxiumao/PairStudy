package com.pairstudy.service;
import com.pairstudy.entity.UploadedImage;
import com.pairstudy.exception.AppException;
import com.pairstudy.mapper.UploadMapper;
import com.pairstudy.security.CurrentUser;
import com.pairstudy.util.StudyDayUtil;
import com.pairstudy.vo.Views.Upload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
@Service
public class UploadService {
 private final UploadMapper mapper; private final PairService pairs; private final CurrentUser current; private final StudyDayUtil time; private final Path root;
 public UploadService(UploadMapper mapper,PairService pairs,CurrentUser current,StudyDayUtil time,@Value("${app.upload-dir}") String directory) throws IOException {
   this.mapper=mapper; this.pairs=pairs; this.current=current; this.time=time; root=Path.of(directory).toAbsolutePath().normalize().resolve("checkin"); Files.createDirectories(root);
 }
 public Upload upload(MultipartFile file) throws IOException {
   long group=pairs.requireGroup().id;
   if(file.isEmpty() || file.getSize()>8L*1024*1024) throw new AppException(400,"请选择不超过 8 MB 的图片");
   byte[] data=file.getBytes(); String type=detect(data);
   String ext=type.equals("image/jpeg")?"jpg":type.substring(6);
   String original=Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);
   if(!original.matches(".*\\.(jpg|jpeg|png|webp)$")) throw new AppException(400,"仅支持 JPG、PNG、WebP 图片");
   var u=new UploadedImage(); u.userId=current.id(); u.groupId=group; u.filename=UUID.randomUUID()+"."+ext;
   u.imageUrl="/uploads/checkin/"+u.filename; u.mediaType=type; u.createdAt=time.now(); Path path=root.resolve(u.filename);
   Files.write(path,data,StandardOpenOption.CREATE_NEW);
   try { mapper.insert(u); } catch(RuntimeException e) { Files.deleteIfExists(path); throw e; }
   return new Upload(u.id,u.imageUrl);
 }
 public UploadedImage accessible(String filename) {
   if(!filename.matches("[a-f0-9-]{36}\\.(jpg|png|webp)")) throw new AppException(404,"图片不存在");
   var u=mapper.accessible(filename,pairs.requireGroup().id,current.id()); if(u==null) throw new AppException(404,"图片不存在"); return u;
 }
 public Resource resource(UploadedImage u) { var r=new FileSystemResource(root.resolve(u.filename)); if(!r.exists()) throw new AppException(404,"图片文件不存在"); return r; }
 static String detect(byte[] b) {
   if(b.length>=12 && (b[0]&255)==255 && (b[1]&255)==216 && (b[2]&255)==255) return "image/jpeg";
   if(b.length>=24 && Arrays.equals(Arrays.copyOf(b,8),new byte[]{(byte)137,80,78,71,13,10,26,10})) return "image/png";
   if(b.length>=16 && b[0]=='R' && b[1]=='I' && b[2]=='F' && b[3]=='F' && b[8]=='W' && b[9]=='E' && b[10]=='B' && b[11]=='P') return "image/webp";
   throw new AppException(400,"文件内容不是支持的图片格式");
 }
}
