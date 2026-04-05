package com.cube.popol.global.custom;

import java.util.ArrayList;
import java.util.Collection;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.cube.popol.domain.user.entity.UserEntity;
import com.cube.popol.domain.user.enums.UserRole;

public class CustomUserDetails implements UserDetails {
  private String userId;
  private String password;
  private UserRole role;

  public CustomUserDetails(UserEntity userEntity) {
    this.userId = userEntity.getUserId();
    this.password = userEntity.getPassword();
    this.role = userEntity.getRole();
  }

  public CustomUserDetails(String userId, String role) {
    this.userId = userId;
    this.role = UserRole.fromString(role);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Collection<GrantedAuthority> collection = new ArrayList<>();

    collection.add(new GrantedAuthority() {
      @Override
      public @Nullable String getAuthority() {
        return role.getKey();
      }
    });
    return collection;
  }

  @Override
  public String getUsername() {
    return userId;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
