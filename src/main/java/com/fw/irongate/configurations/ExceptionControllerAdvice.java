package com.fw.irongate.configurations;

import com.fw.irongate.web.responses.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionControllerAdvice {

  @ExceptionHandler({IllegalArgumentException.class})
  @ResponseBody
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
  }

  @ExceptionHandler({MethodArgumentNotValidException.class})
  @ResponseBody
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    return ResponseEntity.badRequest()
        .body(
            new ErrorResponse(e.getBindingResult().getAllErrors().getFirst().getDefaultMessage()));
  }
}
