package com.cube.popol.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cube.popol.domain.auth.dto.SignUpFailResponseDTO;
import com.cube.popol.domain.user.dto.UserDTO;
import com.cube.popol.domain.user.service.UserService;
import com.cube.popol.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;

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
}
