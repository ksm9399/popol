package com.cube.popol.domain.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.cube.popol.domain.user.dto.UserDTO;
import com.cube.popol.global.jwt.JwtProvider;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final BCryptPasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtProvider jwtProvider;

  public String encodePassword(String rawPassword) {
    return passwordEncoder.encode(rawPassword);
  }

  public void userLogin(
    UserDTO userDTO,
    HttpServletResponse response
  ) {
    // 인증 시도
    Authentication authentication = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(userDTO.getUserId(), userDTO.getPassword())
    );

    // 토큰 생성 및 쿠키 설정
    String token = jwtProvider.createToken(authentication);
    jwtProvider.addTokenToCookie(response, token);
  }
}
