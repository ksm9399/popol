package com.cube.popol.domain.user.dto;

import com.cube.popol.domain.user.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString
public class UserDTO {
  private String userId;
  private String password;
  private UserRole role;
  private String userEmail;
  private String userNickname;

  // 이메일 인증코드
  private String verificationCode;

  // 비밀번호 형식 검사
  @JsonIgnore
  public boolean isPasswordValid() {
    // 비밀번호는 최소 8자 이상이어야 하며, 영문 대소문자, 숫자, 특수문자를 포함
    String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    if (password == null) return false;
    return password.matches(passwordPattern);
  }
}
