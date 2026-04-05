package com.cube.popol.domain.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cube.popol.domain.auth.dto.SignUpFailResponseDTO;
import com.cube.popol.domain.auth.service.AuthService;
import com.cube.popol.domain.user.dto.UserDTO;
import com.cube.popol.domain.user.service.UserService;
import com.cube.popol.global.response.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;
  private final AuthService authService;

  /* 회원가입 */
  @PostMapping("/sign-up")
  public ResponseEntity<ApiResponse<?>> postSignUp(
    @RequestBody UserDTO userDTO
  ) {

    // 아이디 중복 검사
    if (userService.existsByUserId(userDTO)) {
      return ResponseEntity.badRequest().body(
        new ApiResponse<>(false, "이미 존재하는 아이디입니다.", new SignUpFailResponseDTO(userDTO.getUserId()))
      );
    }
    else {
      // 비밀번호 형식 검사
      if (!userDTO.isPasswordValid()) {
        return ResponseEntity.badRequest().body(
          new ApiResponse<>(false, "비밀번호 형식이 올바르지 않습니다.", new SignUpFailResponseDTO(userDTO.getUserId()))
        );
      }

      userService.saveUser(userDTO);
      return ResponseEntity.ok(new ApiResponse<>(true, "회원가입 성공", null));
    }
  }

  /* 로그인 */
  @PostMapping("/sign-in")
  public ResponseEntity<ApiResponse<?>> postSignIn(
    @RequestBody UserDTO userDTO,
    HttpServletResponse response
  ) {

    try {
      authService.userLogin(userDTO, response);
      return ResponseEntity.ok(new ApiResponse<>(true, "로그인 성공", null));
    } catch (BadCredentialsException e) {
        // 비밀번호가 틀린 경우
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse<>(false, "아이디 또는 비밀번호가 일치하지 않습니다.", null));
    } catch (InternalAuthenticationServiceException e) {
        // 아이디가 존재하지 않거나 시스템 에러
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse<>(false, "존재하지 않는 사용자입니다.", null));
    } catch (Exception e) {
        // 기타 서버 에러
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiResponse<>(false, "서버 오류가 발생했습니다.", null));
    }
  }
}
