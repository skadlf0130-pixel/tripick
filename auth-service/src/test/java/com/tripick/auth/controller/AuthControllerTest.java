package com.tripick.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripick.auth.repository.PasswordResetTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트마다 DB 변경사항을 롤백해 서로 독립적으로 동작하게 함
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    private void register(String email, String password, String name) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "email", email, "password", password, "name", name))));
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email, "password", password))))
                .andReturn();
        return result.getResponse().getContentAsString();
    }

    private String extractField(String json, String field) throws Exception {
        JsonNode node = objectMapper.readTree(json).path("data").path(field);
        return node.asText();
    }

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
    @DisplayName("회원가입 실패 - 이메일 중복 409 반환")
    void register_duplicateEmail_returns409() throws Exception {
        register("dup@tripick.com", "Password1!", "중복유저");

        var request = Map.of(
                "email", "dup@tripick.com",
                "password", "Password1!",
                "name", "다른이름"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_EMAIL"));
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
    @DisplayName("이메일 중복 확인 - 가입 전 false, 가입 후 true")
    void checkEmail_reflectsRegistration() throws Exception {
        mockMvc.perform(get("/api/auth/check-email").param("email", "checkemail@tripick.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isDuplicate").value(false));

        register("checkemail@tripick.com", "Password1!", "체크유저");

        mockMvc.perform(get("/api/auth/check-email").param("email", "checkemail@tripick.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isDuplicate").value(true));
    }

    // ── 로그인 ──

    @Test
    @DisplayName("로그인 성공 - 200, 토큰 반환")
    void login_validRequest_returns200WithTokens() throws Exception {
        register("login@tripick.com", "Password1!", "로그인유저");

        var request = Map.of("email", "login@tripick.com", "password", "Password1!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호 401 반환")
    void login_wrongPassword_returns401() throws Exception {
        register("wrongpw@tripick.com", "Password1!", "유저");

        var request = Map.of("email", "wrongpw@tripick.com", "password", "WrongPass1!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 형식 오류 400 반환")
    void login_invalidEmail_returns400() throws Exception {
        var request = Map.of("email", "not-email", "password", "Password1!");

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
        register("logout@tripick.com", "Password1!", "로그아웃유저");
        String refreshToken = extractField(login("logout@tripick.com", "Password1!"), "refreshToken");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("로그아웃 실패 - 비인증 403 반환")
    void logout_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", "anything"))))
                .andExpect(status().isForbidden());
    }

    // ── 토큰 재발급 ──

    @Test
    @DisplayName("토큰 재발급 성공 - 200 반환, 기존 토큰은 폐기됨")
    void tokenRefresh_validRequest_returns200() throws Exception {
        register("refresh@tripick.com", "Password1!", "재발급유저");
        String refreshToken = extractField(login("refresh@tripick.com", "Password1!"), "refreshToken");

        mockMvc.perform(post("/api/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists());

        // 같은 refresh token 재사용 시 이미 폐기되어 실패해야 함
        mockMvc.perform(post("/api/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 유효하지 않은 토큰 401 반환")
    void tokenRefresh_invalidToken_returns401() throws Exception {
        var request = Map.of("refreshToken", "not-a-real-jwt");

        mockMvc.perform(post("/api/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("REFRESH_TOKEN_INVALID"));
    }

    // ── 비밀번호 재설정 ──

    @Test
    @DisplayName("비밀번호 재설정 메일 발송 - 가입 여부와 무관하게 200 반환")
    void forgotPassword_anyEmail_returns200() throws Exception {
        mockMvc.perform(post("/api/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "unknown@tripick.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("비밀번호 재설정 완료 - 발급된 토큰으로 200 반환, 이후 로그인은 새 비밀번호로만 가능")
    void resetPassword_validToken_returns200() throws Exception {
        register("reset@tripick.com", "OldPass1!", "재설정유저");

        mockMvc.perform(post("/api/auth/password/forgot")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", "reset@tripick.com"))));

        String token = passwordResetTokenRepository.findAll().stream()
                .reduce((first, second) -> second) // 가장 마지막에 생성된 토큰
                .orElseThrow()
                .getToken();

        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", token, "newPassword", "NewPass1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "reset@tripick.com", "password", "NewPass1!"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비밀번호 재설정 실패 - 존재하지 않는 토큰 401 반환")
    void resetPassword_invalidToken_returns401() throws Exception {
        var request = Map.of("token", "bogus-token", "newPassword", "NewPassword1!");

        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
    }
}
