package com.cube.popol.global.jwt;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.cube.popol.global.custom.CustomUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtProvider {

  @Value("${jwt.secret}")
  private String SECRET_KEY;

  @Value("${jwt.access-expire}")
  private long ACCESS_TOKEN_EXPIRE;

  @Value("${jwt.refresh-expire}")
  private long REFRESH_TOKEN_EXPIRE;

  private Key key;

  // 한번만 Key 생성
  @PostConstruct
  public void init() {
    key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
  }

  // access 토큰 생성
  public String createAccessToken(Authentication authentication) {
    CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
    String role = user.getAuthorities().iterator().next().getAuthority();

    return Jwts.builder()
      .setSubject(user.getUsername())
      .claim("role", role)
      .setIssuedAt(new Date())
      .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE))
      .signWith(key)
      .compact();
  }

  // refreshToken 생성
  public String createRefreshToken(Authentication authentication) {
    CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
    String role = user.getAuthorities().iterator().next().getAuthority();

    return Jwts.builder()
      .setSubject(user.getUsername())
      .claim("role", role)
      .setIssuedAt(new Date())
      .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE)) // refreshToken은 7일
      .signWith(key)
      .compact();
  }

  // 재발급 토큰 생성 (userId, role로 생성)
  public String createNewToken(String userId, String role, String tokenType) {
    long expireTime = "access".equals(tokenType) ? ACCESS_TOKEN_EXPIRE : REFRESH_TOKEN_EXPIRE;

    return Jwts.builder()
      .setSubject(userId)
      .claim("role", role)
      .setIssuedAt(new Date())
      .setExpiration(new Date(System.currentTimeMillis() + expireTime))
      .signWith(key)
      .compact();
  }

  // 토큰에서 인증 객체 생성
  public Authentication getAuthentication(String token) {
    Claims claims = parseClaims(token);

    String userId = claims.getSubject();
    String role = claims.get("role", String.class);

    CustomUserDetails userDetails = new CustomUserDetails(userId, role);

    return new UsernamePasswordAuthenticationToken(
      userDetails,
      null,
      Collections.singletonList(new SimpleGrantedAuthority(role)));
  }

  // 토큰 검증
  public void validateToken(String token) {
    try {
      parseClaims(token);
    } catch (SecurityException | MalformedJwtException e) {
      log.info("잘못된 JWT 서명입니다.");
      throw new RuntimeException("INVALID_TOKEN", e);
    } catch (ExpiredJwtException e) {
      log.info("만료된 JWT 토큰입니다.");
      throw new RuntimeException("EXPIRED_TOKEN", e);
    } catch (UnsupportedJwtException e) {
      log.info("지원되지 않는 JWT 토큰입니다.");
      throw new RuntimeException("UNSUPPORTED_TOKEN", e);
    } catch (IllegalArgumentException e) {
      log.info("JWT 토큰이 잘못되었습니다.");
      throw new RuntimeException("INVALID_TOKEN", e);
    }
  }

  // Claims 파싱
  private Claims parseClaims(String token) {
    return Jwts.parserBuilder()
      .setSigningKey(key)
      .build()
      .parseClaimsJws(token)
      .getBody();
  }

  public void addTokenToCookie(HttpServletResponse response, String token, String cookieName) {
    Cookie cookie = new Cookie(cookieName, token);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");

    // 토큰 이름에 따라 만료 시간 설정
    long expire = cookieName.equals("refreshToken") ? REFRESH_TOKEN_EXPIRE : ACCESS_TOKEN_EXPIRE;
    cookie.setMaxAge((int) (expire / 1000));

    response.addCookie(cookie);
  }

  // 로그아웃시 토큰 쿠키 삭제
  public void deleteTokenCookie(HttpServletResponse response, String cookieName) {
    Cookie cookie = new Cookie(cookieName, null);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(0); // 쿠키 즉시 삭제
    response.addCookie(cookie);
  }

  public long getRefreshTokenExpireTime() {
    return REFRESH_TOKEN_EXPIRE;
  }

  public String getUserIdFromToken(String token) {
    Claims claims = parseClaims(token);
    return claims.getSubject();
  }

  public String getRoleFromToken(String token) {
    try {
      Claims claims = parseClaims(token);
      return claims.get("role", String.class);
    } catch (ExpiredJwtException e) {
      // 토큰이 만료된 경우에도 Claims를 얻을 수 있음
      Claims claims = e.getClaims();
      return claims.get("role", String.class);
    }
  }
}
