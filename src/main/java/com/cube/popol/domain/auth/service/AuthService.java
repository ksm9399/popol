package com.cube.popol.domain.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cube.popol.domain.user.dto.UserDTO;
import com.cube.popol.global.jwt.JwtProvider;
import com.cube.popol.global.redis.RedisRepository;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtProvider jwtProvider;
  private final RedisRepository redisRepository;

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
    String token = jwtProvider.createAccessToken(authentication);
    String refreshToken = jwtProvider.createRefreshToken(authentication);

    // reids에 refreshToken 저장
    // userId 추출
    String userId = authentication.getName();
    redisRepository.saveRefreshToken(
      userId,
      refreshToken,
      jwtProvider.getRefreshTokenExpireTime()
    );

    jwtProvider.addTokenToCookie(response, token, "accessToken");
    jwtProvider.addTokenToCookie(response, refreshToken, "refreshToken");
  }

  public void userLogout(HttpServletResponse response, String userId) {
    // redis에서 refreshToken 삭제
    redisRepository.deleteRefreshToken(userId);

    jwtProvider.deleteTokenCookie(response, "accessToken");
    jwtProvider.deleteTokenCookie(response, "refreshToken");
  }

  @Transactional
  public void reissueTokens(
    HttpServletResponse response,
    String refreshToken
  ) {
    try {
      // 토큰 자체의 유효성 검증 (만료 여부 및 서명 확인)
      jwtProvider.validateToken(refreshToken);

      // 토큰에서 유저 식별자(userId) 추출 후 Redis에서 저장된 리프레시 토큰조회
      String userId = jwtProvider.getUserIdFromToken(refreshToken);
      String savedToken = redisRepository.getRefreshToken(userId);

      // Redis에 토큰이 없거나, 클라이언트가 보낸 토큰과 일치하지 않으면 예외 발생
      if (savedToken == null || !savedToken.equals(refreshToken)) {
        // 일치하지 않을 경우 탈취된 토큰일 가능성이 있으므로 Redis 토큰 삭제 처리
        redisRepository.deleteRefreshToken(userId);
        throw new RuntimeException("리프레시 토큰이 일치하지 않습니다.");
      }

      // Refresh Token에서 role 추출
      String role = jwtProvider.getRoleFromToken(refreshToken);

      // 5. 새로운 토큰 쌍 생성 (Access Token & Refresh Token)
      String newAccessToken = jwtProvider.createNewToken(userId, role, "access");
      String newRefreshToken = jwtProvider.createNewToken(userId, role, "refresh");

      // 6. Redis 갱신 (기존 토큰 덮어쓰기)
      redisRepository.saveRefreshToken(
        userId,
        newRefreshToken,
        jwtProvider.getRefreshTokenExpireTime()
      );

      jwtProvider.addTokenToCookie(response, newAccessToken, "accessToken");
      jwtProvider.addTokenToCookie(response, newRefreshToken, "refreshToken");
    } catch (Exception e) {
      // 에러 로깅 후 다시 던져서 컨트롤러가 401 응답을 보내게 함
      log.info("토큰 재발급 중 오류 발생: {}", e.getMessage());
      throw e;
    }

  }
}
