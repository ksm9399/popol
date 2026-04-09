package com.cube.popol.global.jwt;

import java.util.Collections;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
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

  private SecretKey key; // Key -> SecretKey

  @PostConstruct
  public void init() {
    key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
  }

  // access 토큰 생성
  public String createAccessToken(Authentication authentication) {
    CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
    String role = user.getAuthorities().iterator().next().getAuthority();

    return Jwts.builder()
      .subject(user.getUsername()) // setSubject -> subject
      .claim("role", role)
      .issuedAt(new Date()) // setIssuedAt -> issuedAt
      .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE)) // setExpiration -> expiration
      .signWith(key)
      .compact();
  }

  // refreshToken 생성
  public String createRefreshToken(Authentication authentication) {
    CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
    String role = user.getAuthorities().iterator().next().getAuthority();

    return Jwts.builder()
      .subject(user.getUsername())
      .claim("role", role)
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE))
      .signWith(key)
      .compact();
  }

  // 재발급 토큰 생성
  public String createNewToken(String userId, String role, String tokenType) {
    long expireTime = "access".equals(tokenType) ? ACCESS_TOKEN_EXPIRE : REFRESH_TOKEN_EXPIRE;

    return Jwts.builder()
      .subject(userId)
      .claim("role", role)
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + expireTime))
      .signWith(key)
      .compact();
  }

  public Authentication getAuthentication(String token) {
    Claims claims = parseClaims(token);

    String userId = claims.getSubject();
    String role = claims.get("role", String.class);

    CustomUserDetails userDetails = new CustomUserDetails(userId, role);

    return new UsernamePasswordAuthenticationToken(
      userDetails,
      null,
      Collections.singletonList(new SimpleGrantedAuthority(role))
    );
  }

  public void validateToken(String token) {
    try {
      parseClaims(token);
    } catch (MalformedJwtException e) {
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

  // Claims 파싱 (0.13.0 스타일)
  private Claims parseClaims(String token) {
    return Jwts.parser() // parserBuilder() -> parser()
      .verifyWith(key) // setSigningKey() -> verifyWith()
      .build()
      .parseSignedClaims(token) // parseClaimsJws() -> parseSignedClaims()
      .getPayload(); // getBody() -> getPayload()
  }

  public void addTokenToCookie(HttpServletResponse response, String token, String cookieName) {
    long expire = cookieName.equals("refreshToken") ? REFRESH_TOKEN_EXPIRE : ACCESS_TOKEN_EXPIRE;
    ResponseCookie responseCookie = ResponseCookie.from(cookieName, token)
      .httpOnly(true)
      .secure(true)
      .path("/")
      .sameSite("Lax")
      .maxAge(expire / 1000)
      .build();

    response.addHeader("Set-Cookie", responseCookie.toString());
  }

  public void deleteTokenCookie(HttpServletResponse response, String cookieName) {
    ResponseCookie responseCookie = ResponseCookie.from(cookieName, "")
      .httpOnly(true)
      .secure(true)
      .path("/")
      .sameSite("Lax")
      .maxAge(0)
      .build();

    response.addHeader("Set-Cookie", responseCookie.toString());
  }

  public long getRefreshTokenExpireTime() {
    return REFRESH_TOKEN_EXPIRE;
  }

  public String getUserIdFromToken(String token) {
    return parseClaims(token).getSubject();
  }

  public String getRoleFromToken(String token) {
    try {
      return parseClaims(token).get("role", String.class);
    } catch (ExpiredJwtException e) {
      // 만료된 경우에도 페이로드에서 데이터를 가져오려면 getPayload() 사용
      return e.getClaims().get("role", String.class);
    }
  }
}