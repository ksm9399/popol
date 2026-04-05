package com.cube.popol.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cube.popol.domain.user.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

  boolean existsByUserId(String userId);
  UserEntity findByUserId(String userId);
}
