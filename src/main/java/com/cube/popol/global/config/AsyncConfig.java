package com.cube.popol.global.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean("mailExecutor")
  public Executor mailExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    executor.setCorePoolSize(5);   // 기본 쓰레드 수
    executor.setMaxPoolSize(10);   // 최대 쓰레드 수
    executor.setQueueCapacity(50); // 대기 큐

    executor.setThreadNamePrefix("mail-");

    executor.initialize();
    return executor;
  }
}
