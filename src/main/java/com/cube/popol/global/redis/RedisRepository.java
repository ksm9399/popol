package com.cube.popol.global.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

  private final StringRedisTemplate redisTemplate;

  private static final String PREFIX = "RT:";

  // 저장
  public void saveRefreshToken(String userId, String refreshToken, long expiration) {
    redisTemplate.opsForValue().set(PREFIX + userId, refreshToken, expiration, TimeUnit.MILLISECONDS);
  }

  // 조회
  public String getRefreshToken(String userId) {
    return redisTemplate.opsForValue().get(PREFIX + userId);
  }

  // 삭제
  public void deleteRefreshToken(String userId) {
    redisTemplate.delete(PREFIX + userId);
  }
}
