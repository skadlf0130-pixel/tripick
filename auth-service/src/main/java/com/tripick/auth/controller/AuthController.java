package com.tripick.auth.controller;

import com.tripick.auth.dto.request.*;
import com.tripick.auth.service.AuthService;
import com.tripick.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "회원가입")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(authService.register(request)));
    }

    @GetMapping("/check-email")
    @Operation(summary = "이메일 중복 확인")
    public ResponseEntity<ApiResponse<?>> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("isDuplicate", authService.checkEmailDuplicate(email))));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody TokenRefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok("로그아웃이 완료되었습니다."));
    }

    @PostMapping("/token/refresh")
    @Operation(summary = "토큰 재발급")
    public ResponseEntity<ApiResponse<?>> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(request)));
    }

    @PostMapping("/password/forgot")
    @Operation(summary = "비밀번호 재설정 메일 발송")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody PasswordForgotRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("비밀번호 재설정 링크를 이메일로 발송했습니다."));
    }

    @PostMapping("/password/reset")
    @Operation(summary = "비밀번호 재설정")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("비밀번호가 변경되었습니다."));
    }
}
