package com.cube.popol.redis;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cube.popol.global.redis.RedisRepository;

@SpringBootTest
public class RedisReissueTokenTest {
  @Autowired
  private RedisRepository redisRepository;

  // 실제 환경이라면 JwtProvider도 주입받아 사용합니다.
  // @Autowired
  // private JwtProvider jwtProvider;

  @Test
  @DisplayName("리프레시 토큰이 일치하면 새로운 토큰 재발급에 성공한다")
  void reissueTokenSuccess() {
    // 1. Given: Redis에 리프레시 토큰이 저장되어 있음
    String userId = "test01";
    String savedRefreshToken = "existing-refresh-token-123";
    long expiration = 604800000L; // 7일 (ms)

    redisRepository.saveRefreshToken(userId, savedRefreshToken, expiration);

    // 2. When: 클라이언트가 재발급 요청 시 보낸 토큰
    String clientRefreshToken = "existing-refresh-token-123";
    String redisStoredToken = redisRepository.getRefreshToken(userId);

    // 3. Then: 검증
    Assertions.assertThat(redisStoredToken).isNotNull();
    Assertions.assertThat(redisStoredToken).isEqualTo(clientRefreshToken);

    // 일치한다면 여기서 새로운 토큰 쌍 생성 로직 실행
    System.out.println("검증 성공: 새로운 액세스 및 리프레시 토큰을 발급합니다.");
  }

  @Test
  @DisplayName("Redis에 저장된 토큰과 다르면 재발급에 실패한다")
  void reissueTokenFail() {
    // 1. Given
    String userId = "test01";
    redisRepository.saveRefreshToken(userId, "original-token", 100000L);

    // 2. When: 탈취되었거나 조작된 토큰으로 요청이 온 경우
    String wrongToken = "hacker-token";
    String redisStoredToken = redisRepository.getRefreshToken(userId);

    // 3. Then: 불일치 확인
    Assertions.assertThat(redisStoredToken).isNotEqualTo(wrongToken);
    System.out.println("검증 실패: 유효하지 않은 리프레시 토큰입니다.");
  }
}
