package com.tripick.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * [common] JWT 토큰 생성·검증 유틸리티
 * - auth-service: 로그인 성공 시 토큰 발급
 * - gateway-service: 요청마다 토큰 검증 후 X-User-Id 헤더로 변환
 * Spring @Component가 아닌 순수 클래스 → 각 서비스 JwtConfig에서 @Bean으로 직접 등록
 */
@Slf4j
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtTokenProvider(String secret, long accessExpiration, long refreshExpiration) {
        // HS256 서명용 키 생성 (최소 256bit 필요)
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    // subject = userId, claim에 role 포함 (gateway에서 X-User-Role 헤더로 전달)
    public String createAccessToken(Long userId, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(secretKey)
                .compact();
    }

    // 리프레시 토큰은 role 불포함 - 재발급 요청 시 DB에서 role 재조회
    // jti(랜덤 UUID)를 넣어 같은 초(iat)에 재발급해도 토큰 문자열이 항상 달라지게 함 (DB unique 제약·로테이션 무효화에 필요)
    public String createRefreshToken(Long userId) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // 서명 검증 + 만료 여부 통합 확인 (gateway 필터 메인 검증)
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[JWT] 토큰 만료: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JWT] 유효하지 않은 토큰: {}", e.getMessage());
        }
        return false;
    }

    // validateToken이 false일 때 만료 vs 위조 구분용 (에러 메시지 분기)
    // 만료가 아닌 다른 모든 오류(서명 불일치, 형식 손상 등)는 false(= 위조/무효)로 처리
    public boolean isExpired(String token) {
        try {
            parseClaims(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
