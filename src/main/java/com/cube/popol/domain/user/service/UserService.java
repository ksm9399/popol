package com.cube.popol.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cube.popol.domain.auth.service.AuthService;
import com.cube.popol.domain.user.dto.UserDTO;
import com.cube.popol.domain.user.entity.UserEntity;
import com.cube.popol.domain.user.enums.UserRole;
import com.cube.popol.domain.user.repository.UserRepository;
import com.cube.popol.global.redis.RedisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final AuthService authService;
  private final RedisRepository redisRepository;

  // 아이디 중복 검사
  public boolean existsByUserId(UserDTO userDTO) {
    return userRepository.existsByUserId(userDTO.getUserId());
  }

  // 회원 저장
  @Transactional
  public void saveUser(UserDTO userDTO) {
    // 비밀번호 암호화
    String encodedPassword = authService.encodePassword(userDTO.getPassword());

    UserEntity userEntity = UserEntity.builder()
      .userId(userDTO.getUserId())
      .password(encodedPassword)
      .role(UserRole.USER)
      .userEmail(userDTO.getUserEmail())
      .userNickname(userDTO.getUserNickname())
      .build();

    userRepository.save(userEntity);

    // redis 인증코드 제거
    redisRepository.deleteEmailAuthCode(userDTO.getUserId());
  }
}
