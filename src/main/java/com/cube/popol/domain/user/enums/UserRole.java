package com.cube.popol.domain.user.enums;

public enum UserRole {
  GUEST("ROLE_GUEST"),
  USER("ROLE_USER"),
  ADMIN("ROLE_ADMIN");

  private final String key;

  UserRole(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public static UserRole fromString(String value) {
    for (UserRole r : UserRole.values()) {
      if (r.name().equalsIgnoreCase(value) || r.getKey().equalsIgnoreCase(value)) {
        return r;
      }
    }
    throw new IllegalArgumentException("Unknown role: " + value);
}
}
