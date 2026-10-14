package com.sinnarmobile;

import java.util.LinkedHashMap;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class Errors {
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    ResponseEntity<Api<Object>> method(Exception e) { return ResponseEntity.status(405).body(Api.error("This HTTP method is not supported.",null)); }
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    ResponseEntity<Api<Object>> missing(Exception e) { return ResponseEntity.status(404).body(Api.error("Resource not found.",null)); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Api<Object>> validation(MethodArgumentNotValidException e) {
        var fields=new LinkedHashMap<String,String>();
        e.getBindingResult().getFieldErrors().forEach(f->fields.put(f.getField(),f.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Api.error("Please check the supplied fields.",fields));
    }
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Api<Object>> status(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Api.error(e.getReason(),null));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Api<Object>> conflict(DataIntegrityViolationException e) {
        return ResponseEntity.status(409).body(Api.error("This name already exists or the record is still in use.",null));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class,IllegalArgumentException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,org.springframework.web.bind.MissingServletRequestParameterException.class,org.springframework.web.multipart.support.MissingServletRequestPartException.class})
    ResponseEntity<Api<Object>> bad(Exception e) { return ResponseEntity.badRequest().body(Api.error("Invalid request or field value.",null)); }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Api<Object>> large(Exception e) { return ResponseEntity.status(413).body(Api.error("Image must be smaller than 5 MB.",null)); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<Api<Object>> unknown(Exception e) {
        LoggerFactory.getLogger(Errors.class).error("Request failed",e);
        return ResponseEntity.internalServerError().body(Api.error("Something went wrong. Please try again.",null));
    }
}
