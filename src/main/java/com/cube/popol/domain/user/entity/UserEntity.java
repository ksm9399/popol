package com.cube.popol.domain.user.entity;

import java.time.LocalDateTime;

import com.cube.popol.domain.user.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "no", comment = "사용자 고유 번호 (자동 생성)")
  private Long no;

  @Column(unique = true, length = 30, nullable = false, comment = "사용자 ID (로그인에 사용)")
  private String userId;

  @Column(nullable = false, comment = "암호화된 비밀번호 저장")
  private String password;

  @Column(nullable = false, comment = "사용자 역할")
  @Enumerated(EnumType.STRING)
  private UserRole role;

  @Column(nullable = false, length = 50, comment = "사용자 이메일")
  private String userEmail;

  @Column(unique = true, nullable = false, length = 50, comment = "사용자 닉네임")
  private String userNickname;

  @Column(nullable = false, updatable = false, comment = "생성일")
  private LocalDateTime createdAt;

  @Column(nullable = false, comment = "수정일")
  private LocalDateTime updatedAt;

  @Builder
  public UserEntity(
    String userId,
    String password,
    UserRole role,
    String userEmail,
    String userNickname
  ) {
    this.userId = userId;
    this.password = password;
    this.role = role;
    this.userEmail = userEmail;
    this.userNickname = userNickname;
  }

  @PrePersist
  public void prePersist() {
    if (this.role == null) this.role = UserRole.USER;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
