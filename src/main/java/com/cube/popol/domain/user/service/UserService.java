package com.cube.popol.domain.user.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.cube.popol.domain.user.dto.UserDTO;
import com.cube.popol.domain.user.entity.UserEntity;
import com.cube.popol.domain.user.enums.UserRole;
import com.cube.popol.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  // 아이디 중복 검사
  public boolean existsByUserId(UserDTO userDTO) {
    return userRepository.existsByUserId(userDTO.getUserId());
  }

  // 회원 저장
  public void saveUser(UserDTO userDTO) {
    String encodedPassword = passwordEncoder.encode(userDTO.getPassword());

    UserEntity userEntity = UserEntity.builder()
      .userId(userDTO.getUserId())
      .password(encodedPassword)
      .role(UserRole.USER)
      .build();

    userRepository.save(userEntity);
  }
}
