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

  @Value("${jwt.expire}")
  private long ACCESS_TOKEN_EXPIRE;

  private Key key;

  // 한번만 Key 생성
  @PostConstruct
  public void init() {
    key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
  }

  // 토큰 생성
  public String createToken(Authentication authentication) {
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
  public boolean validateToken(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (SecurityException | MalformedJwtException e) {
      log.info("잘못된 JWT 서명입니다.");
    } catch (ExpiredJwtException e) {
      log.info("만료된 JWT 토큰입니다.");
    } catch (UnsupportedJwtException e) {
      log.info("지원되지 않는 JWT 토큰입니다.");
    } catch (IllegalArgumentException e) {
      log.info("JWT 토큰이 잘못되었습니다.");
    }
    return false;
  }

  // Claims 파싱
  private Claims parseClaims(String token) {
    return Jwts.parserBuilder()
      .setSigningKey(key)
      .build()
      .parseClaimsJws(token)
      .getBody();
  }

  public void addTokenToCookie(HttpServletResponse response, String token) {
    Cookie cookie = new Cookie("accessToken", token);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge((int) (ACCESS_TOKEN_EXPIRE / 1000));
    response.addCookie(cookie);
  }
}
