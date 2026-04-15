package com.cube.popol.global.util;

import java.security.SecureRandom;

public class RandomUtil {

  private static final SecureRandom secureRandom = new SecureRandom();
  private static final String CHARACTERS =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

  public String generateEmailAuthCode(int length) {
    StringBuilder code = new StringBuilder(length);

    for (int i = 0; i < length; i++) {
      int index = secureRandom.nextInt(CHARACTERS.length());
      code.append(CHARACTERS.charAt(index));
    }

    return code.toString();
  }
}
