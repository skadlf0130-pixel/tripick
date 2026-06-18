package com.tripick.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-hs256";
    private static final long ACCESS_EXPIRATION = 3_600_000L;   // 1시간
    private static final long REFRESH_EXPIRATION = 604_800_000L; // 7일

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, ACCESS_EXPIRATION, REFRESH_EXPIRATION);
    }

    @Test
    @DisplayName("액세스 토큰 생성 성공")
    void createAccessToken_success() {
        String token = jwtTokenProvider.createAccessToken(1L, "USER");

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(1L);
        assertThat(jwtTokenProvider.getRole(token)).isEqualTo("USER");
    }

    @Test
    @DisplayName("리프레시 토큰 생성 성공")
    void createRefreshToken_success() {
        String token = jwtTokenProvider.createRefreshToken(1L);

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(1L);
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공")
    void validateToken_validToken_returnsTrue() {
        String token = jwtTokenProvider.createAccessToken(1L, "USER");

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("조작된 토큰 검증 실패")
    void validateToken_tamperedToken_returnsFalse() {
        String token = jwtTokenProvider.createAccessToken(1L, "USER");
        String tampered = token + "tampered";

        assertThat(jwtTokenProvider.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("빈 토큰 검증 실패")
    void validateToken_emptyToken_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("만료되지 않은 토큰 isExpired false 반환")
    void isExpired_freshToken_returnsFalse() {
        String token = jwtTokenProvider.createAccessToken(1L, "USER");

        assertThat(jwtTokenProvider.isExpired(token)).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 isExpired true 반환")
    void isExpired_expiredToken_returnsTrue() {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(SECRET, 1L, 1L);
        String token = shortLivedProvider.createAccessToken(1L, "USER");

        assertThat(shortLivedProvider.isExpired(token)).isTrue();
    }

    @Test
    @DisplayName("getUserId 정상 추출")
    void getUserId_success() {
        String token = jwtTokenProvider.createAccessToken(42L, "ADMIN");

        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("getRole 정상 추출")
    void getRole_success() {
        String token = jwtTokenProvider.createAccessToken(1L, "ADMIN");

        assertThat(jwtTokenProvider.getRole(token)).isEqualTo("ADMIN");
    }
}
