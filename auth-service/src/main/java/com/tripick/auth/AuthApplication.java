package com.tripick.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * [auth-service] 인증 서비스 - 포트 8081
 * 담당: 회원가입, 로그인, JWT 발급, 비밀번호 관리
 * scanBasePackages에 common 포함 → HeaderAuthenticationFilter, GlobalExceptionHandler 자동 등록
 */
@SpringBootApplication(scanBasePackages = {"com.tripick.auth", "com.tripick.common"})
@EnableJpaAuditing
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}