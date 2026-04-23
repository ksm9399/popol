package com.cube.popol.global.error;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cube.popol.global.response.ApiResponse;

@RestControllerAdvice
public class GlobalErrorHandler {

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<?>> handleBadCredentials() {
    return ResponseEntity.status(401).body(
        new ApiResponse<>(false, "아이디 또는 비밀번호가 일치하지 않습니다.", null));
  }

  @ExceptionHandler(InternalAuthenticationServiceException.class)
  public ResponseEntity<ApiResponse<?>> handleUserNotFound() {
    return ResponseEntity.status(401).body(
        new ApiResponse<>(false, "존재하지 않는 사용자입니다.", null));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
    return ResponseEntity.status(500).body(
        new ApiResponse<>(false, "서버 오류가 발생했습니다.", null));
  }
}
