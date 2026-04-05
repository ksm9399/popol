package com.cube.popol.global.custom;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cube.popol.domain.user.entity.UserEntity;
import com.cube.popol.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

    UserEntity userEntity = userRepository.findByUserId(userId);

    if (userEntity != null) {
      return new CustomUserDetails(userEntity);
    }

    throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
  }

}
