package com.cube.popol.redis;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class RedisConnectionTest {

  @Autowired
  private StringRedisTemplate redisTemplate;

  @Test
  @DisplayName("Redis 서버에 정상적으로 연결되는지 테스트")
  void redisConnectionTest() {
    // Given: 테스트 데이터 설정
        String key = "test:connection";
        String value = "success";
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();

        // When: 데이터 저장
        valueOperations.set(key, value);

        // Then: 데이터 조회 및 검증
        String result = valueOperations.get(key);

        System.out.println("--------------------------------");
        System.out.println("조회된 결과: " + result);
        System.out.println("--------------------------------");

        Assertions.assertThat(result).isEqualTo(value);

        // 테스트 완료 후 데이터 삭제 (선택 사항)
        redisTemplate.delete(key);
  }
}
