package com.tripick.auth.controller;

import com.tripick.auth.dto.request.*;
import com.tripick.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    // TODO: AuthService 주입

    @PostMapping("/register")
    @Operation(summary = "회원가입")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequest request) {
        var data = Map.of(
            "userId", 1L,
            "email", request.getEmail(),
            "name", request.getName(),
            "createdAt", LocalDateTime.now().toString()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(data));
    }

    @GetMapping("/check-email")
    @Operation(summary = "이메일 중복 확인")
    public ResponseEntity<ApiResponse<?>> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("isDuplicate", false)));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {
        var data = Map.of(
            "accessToken", "eyJhbGci...",
            "refreshToken", "eyJhbGci...",
            "userId", 1L,
            "name", "홍길동",
            "role", "USER"
        );
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.ok("로그아웃이 완료되었습니다."));
    }

    @PostMapping("/token/refresh")
    @Operation(summary = "토큰 재발급")
    public ResponseEntity<ApiResponse<?>> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        var data = Map.of(
            "accessToken", "eyJhbGci...",
            "refreshToken", "eyJhbGci..."
        );
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @PostMapping("/password/forgot")
    @Operation(summary = "비밀번호 재설정 메일 발송")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody PasswordForgotRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("비밀번호 재설정 링크를 이메일로 발송했습니다."));
    }

    @PostMapping("/password/reset")
    @Operation(summary = "비밀번호 재설정")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("비밀번호가 변경되었습니다."));
    }
}