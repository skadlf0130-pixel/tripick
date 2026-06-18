package com.tripick.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripick.auth.dto.request.LoginRequest;
import com.tripick.auth.dto.request.PasswordForgotRequest;
import com.tripick.auth.dto.request.PasswordResetRequest;
import com.tripick.auth.dto.request.RegisterRequest;
import com.tripick.auth.dto.request.TokenRefreshRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ── 회원가입 ──

    @Test
    @DisplayName("회원가입 성공 - 201 반환")
    void register_validRequest_returns201() throws Exception {
        var request = Map.of(
                "email", "test@tripick.com",
                "password", "Password1!",
                "name", "홍길동",
                "phone", "010-1234-5678"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@tripick.com"));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 형식 오류 400 반환")
    void register_invalidEmail_returns400() throws Exception {
        var request = Map.of(
                "email", "not-an-email",
                "password", "Password1!",
                "name", "홍길동"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_PARAMETER"));
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 형식 오류 400 반환")
    void register_weakPassword_returns400() throws Exception {
        var request = Map.of(
                "email", "test@tripick.com",
                "password", "weakpass",
                "name", "홍길동"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("회원가입 실패 - 이름 미입력 400 반환")
    void register_missingName_returns400() throws Exception {
        var request = Map.of(
                "email", "test@tripick.com",
                "password", "Password1!"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── 이메일 중복 확인 ──

    @Test
    @DisplayName("이메일 중복 확인 성공 - 200 반환")
    void checkEmail_returns200() throws Exception {
        mockMvc.perform(get("/api/auth/check-email")
                        .param("email", "test@tripick.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isDuplicate").value(false));
    }

    // ── 로그인 ──

    @Test
    @DisplayName("로그인 성공 - 200, 토큰 반환")
    void login_validRequest_returns200WithTokens() throws Exception {
        var request = Map.of(
                "email", "test@tripick.com",
                "password", "Password1!"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 형식 오류 400 반환")
    void login_invalidEmail_returns400() throws Exception {
        var request = Map.of(
                "email", "not-email",
                "password", "Password1!"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── 로그아웃 ──

    @Test
    @DisplayName("로그아웃 성공 - 인증된 사용자 200 반환")
    @WithMockUser
    void logout_authenticatedUser_returns200() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("로그아웃 실패 - 비인증 401 반환")
    void logout_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    // ── 토큰 재발급 ──

    @Test
    @DisplayName("토큰 재발급 성공 - 200 반환")
    void tokenRefresh_validRequest_returns200() throws Exception {
        var request = Map.of("refreshToken", "some.refresh.token");

        mockMvc.perform(post("/api/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists());
    }

    // ── 비밀번호 재설정 ──

    @Test
    @DisplayName("비밀번호 재설정 메일 발송 - 200 반환")
    void forgotPassword_validEmail_returns200() throws Exception {
        var request = Map.of("email", "test@tripick.com");

        mockMvc.perform(post("/api/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("비밀번호 재설정 완료 - 200 반환")
    void resetPassword_validRequest_returns200() throws Exception {
        var request = Map.of(
                "token", "reset-token-123",
                "newPassword", "NewPassword1!"
        );

        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
