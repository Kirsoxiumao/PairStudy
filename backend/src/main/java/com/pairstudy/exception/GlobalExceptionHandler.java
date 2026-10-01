package com.pairstudy.exception;
import com.pairstudy.vo.ApiResponse;
import org.slf4j.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.access.AccessDeniedException;
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log=LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private ResponseEntity<ApiResponse<Void>> fail(int code,String msg) { return ResponseEntity.status(code).body(new ApiResponse<>(code,msg,null)); }
    @ExceptionHandler(AppException.class) ResponseEntity<?> app(AppException e) { return fail(e.code,e.getMessage()); }
    @ExceptionHandler({MethodArgumentNotValidException.class,HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class,IllegalArgumentException.class,java.time.DateTimeException.class,
      org.springframework.web.bind.MissingServletRequestParameterException.class,org.springframework.web.multipart.support.MissingServletRequestPartException.class,
      jakarta.validation.ConstraintViolationException.class})
    ResponseEntity<?> invalid(Exception e) { return fail(400,"参数不正确，请检查输入"); }
    @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<?> conflict(Exception e) { return fail(409,"数据已存在或发生冲突，请刷新后重试"); }
    @ExceptionHandler(MaxUploadSizeExceededException.class) ResponseEntity<?> size(Exception e) { return fail(413,"图片不能超过 8 MB"); }
    @ExceptionHandler(NoResourceFoundException.class) ResponseEntity<?> missing(Exception e) { return fail(404,"资源不存在"); }
    @ExceptionHandler(AccessDeniedException.class) ResponseEntity<?> forbidden(Exception e) { return fail(403,"无权访问"); }
    @ExceptionHandler(Exception.class) ResponseEntity<?> unknown(Exception e) { log.error("Request failed",e); return fail(500,"服务器暂时繁忙，请稍后重试"); }
}
