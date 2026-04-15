package com.cube.popol.domain.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cube.popol.domain.auth.dto.EmailResponseDTO;
import com.cube.popol.domain.auth.dto.SignUpFailResponseDTO;
import com.cube.popol.domain.auth.service.AuthService;
import com.cube.popol.domain.user.dto.UserDTO;
import com.cube.popol.domain.user.service.UserService;
import com.cube.popol.global.custom.CustomUserDetails;
import com.cube.popol.global.response.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

  private final UserService userService;
  private final AuthService authService;

  /* 회원가입 */
  @PostMapping("/sign-up")
  public ResponseEntity<ApiResponse<?>> postSignUp(
    @RequestBody UserDTO userDTO
  ) {
    // 아이디 중복 검사
    if (userService.existsByUserId(userDTO)) {
      return ResponseEntity.badRequest().body(
        new ApiResponse<>(false, "이미 존재하는 아이디입니다.", null)
      );
    } else {
      // 비밀번호 형식 검사
      if (!userDTO.isPasswordValid()) {
        return ResponseEntity.badRequest().body(
          new ApiResponse<>(false, "비밀번호 형식이 올바르지 않습니다.", null)
        );
      }

      userService.saveUser(userDTO);
      return ResponseEntity.ok(new ApiResponse<>(true, "회원가입 성공", null));
    }
  }

  /* 로그인 */
  @PostMapping("/sign-in")
  public ResponseEntity<ApiResponse<?>> postSignIn(
    @RequestBody UserDTO userDTO,
    HttpServletResponse response
  ) {
    try {
      authService.userLogin(userDTO, response);
      return ResponseEntity.ok(new ApiResponse<>(true, "로그인 성공", null));
    } catch (BadCredentialsException e) {
      // 비밀번호가 틀린 경우
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
        new ApiResponse<>(false, "아이디 또는 비밀번호가 일치하지 않습니다.", new SignUpFailResponseDTO(userDTO.getUserId()))
      );
    } catch (InternalAuthenticationServiceException e) {
      // 아이디가 존재하지 않거나 시스템 에러
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
        new ApiResponse<>(false, "존재하지 않는 사용자입니다.", new SignUpFailResponseDTO(userDTO.getUserId()))
      );
    } catch (Exception e) {
      // 기타 서버 에러
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
        new ApiResponse<>(false, "서버 오류가 발생했습니다.", new SignUpFailResponseDTO(userDTO.getUserId()))
      );
    }
  }

  /* 로그아웃 */
  @PostMapping("/sign-out")
  public ResponseEntity<ApiResponse<?>> postSignOut(
    HttpServletResponse response,
    @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    String userId = userDetails.getUsername();
    authService.userLogout(response, userId);
    return ResponseEntity.ok(new ApiResponse<>(true, "로그아웃 성공", null));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<?>> getMyInfo(
    @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    if (userDetails == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
        new ApiResponse<>(false, "인증되지 않은 사용자입니다.", null)
      );
    }

    return ResponseEntity.ok(new ApiResponse<>(true, "내 정보 조회 성공", Map.of(
      "userId", userDetails.getUsername(),
      "role", userDetails.getRole()
    )));
  }

  @PostMapping("/reissue")
  public ResponseEntity<ApiResponse<?>> postReissue(
    @CookieValue(value = "refreshToken", required = false) String refreshToken,
    HttpServletResponse response
  ) {
    if (refreshToken == null || refreshToken.isEmpty()) {
      return ResponseEntity.badRequest().body(
        new ApiResponse<>(false, "리프레시 토큰이 필요합니다.", null)
      );
    }

    try {
      authService.reissueTokens(response, refreshToken);
      return ResponseEntity.ok(new ApiResponse<>(true, "토큰 재발급 성공", null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
        new ApiResponse<>(false, "유효하지 않은 리프레시 토큰입니다.", null)
      );
    }
  }

  @PostMapping("/email/send-code")
  public ResponseEntity<ApiResponse<?>> sendEmailAuthCode(
    @RequestBody UserDTO userDTO
  ) {
    long ttl = authService.sendEmailAuthCode(userDTO);

    EmailResponseDTO resultData = new EmailResponseDTO();
    resultData.setTtl(ttl);

    return ResponseEntity.ok(
      new ApiResponse<>(
        true,
        "입력하신 이메일로 인증코드를 보냈습니다.\n 메일을 받지 못했다면 이메일을 다시 확인해주세요.",
        resultData
      )
    );
  }

  @PostMapping("/email/validate-code")
  public ResponseEntity<ApiResponse<?>> validationEmailAuthCode(
    @RequestBody UserDTO userDTO
  ) {
    String message = "";
    EmailResponseDTO resultData = new EmailResponseDTO();

    try {
      boolean validFlg = authService.validationEmailAuthCode(userDTO);
      resultData.setValidFlg(validFlg);

      if (!validFlg) {
      message = "인증코드가 일치하지 않습니다.";
      }

      return ResponseEntity.ok(
        new ApiResponse<>(
          validFlg,
          message,
          resultData
        )
      );
    } catch (Exception e) {
      log.error("인증코드 검증 에러", e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
        new ApiResponse<>(
          false,
          e.getMessage(),
          null
        )
      );
    }
  }
}
