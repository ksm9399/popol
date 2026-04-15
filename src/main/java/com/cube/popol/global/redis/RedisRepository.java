package com.cube.popol.global.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

  @Value("${mail.auth-code-expiration-seconds}")
  private Long EXPIRATION_MAIL;

  private final StringRedisTemplate redisTemplate;

  private static final String PREFIX_TOKEN = "RT:";
  private static final String PREFIX_EMAIL_AUTH = "EMAIL_AUTH:";

  // 리프레시 토큰 저장
  public void saveRefreshToken(String userId, String refreshToken, long expiration) {
    redisTemplate.opsForValue().set(PREFIX_TOKEN + userId, refreshToken, expiration, TimeUnit.MILLISECONDS);
  }

  // 리프레시 토큰 조회
  public String getRefreshToken(String userId) {
    return redisTemplate.opsForValue().get(PREFIX_TOKEN + userId);
  }

  // 리프레시 토큰 삭제
  public void deleteRefreshToken(String userId) {
    redisTemplate.delete(PREFIX_TOKEN + userId);
  }

  // 이메일 인증 코드 저장
  public void saveEmailAuthCode(String userId, String authCode) {
    redisTemplate.opsForValue().set(PREFIX_EMAIL_AUTH + userId, authCode, EXPIRATION_MAIL, TimeUnit.SECONDS);
  }

  // 이메일 인증 코드 조회
  public String getEmailAuthCode(String userId) {
    return redisTemplate.opsForValue().get(PREFIX_EMAIL_AUTH + userId);
  }

  // 이메일 인증 유효기간 조회
  public Long getEmailAuthCodeExpiration(String userId) {
    return redisTemplate.getExpire(PREFIX_EMAIL_AUTH + userId, TimeUnit.SECONDS);
  }

  // 이메일 인증코드 삭제
  public void deleteEmailAuthCode(String userId) {
    redisTemplate.delete(PREFIX_EMAIL_AUTH + userId);
  }
}
