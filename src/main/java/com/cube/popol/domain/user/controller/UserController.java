package com.cube.popol.domain.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cube.popol.domain.user.dto.UserDTO;
import com.cube.popol.domain.user.service.UserService;
import com.cube.popol.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/check-id")
  public ResponseEntity<ApiResponse<?>> postCheckUserId(
    @RequestBody UserDTO userDTO
  ) {

    if (userService.existsByUserId(userDTO)) {
      return ResponseEntity.ok(new ApiResponse<>(true, "이미 존재하는 아이디입니다.", null));
    } else {
      return ResponseEntity.ok(new ApiResponse<>(false, "사용 가능한 아이디입니다.", null));
    }
  }
}
