package com.cube.popol.global.jwt;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtProvider jwtProvider;

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String token = resolveToken(request);

    try {
      if (token != null) {
        jwtProvider.validateToken(token);

        // var - 타입 추론 키워드
        var authentication = jwtProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (RuntimeException e) {
      SecurityContextHolder.clearContext();

      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json;charset=UTF-8");

      if ("EXPIRED_TOKEN".equals(e.getMessage())) {
        response.setHeader("Token-Error", "EXPIRED");
        response.getWriter().write("{\"message\":\"EXPIRED_TOKEN\"}");
      }
      else {  // 만료된 토큰이 아닌경우 모두 INVALID 처리
        response.setHeader("Token-Error", "INVALID");
        response.getWriter().write("{\"message\":\"INVALID_TOKEN\"}");
      }

      return;
    }

    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();

    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("accessToken".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }

    return null;
  }

  // 필터 적용 제외 (로그인, 토큰 재발급 제외)
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
      String path = request.getRequestURI();
      return path.equals("/api/auth/sign-in") || path.equals("/api/auth/reissue");
  }

}
