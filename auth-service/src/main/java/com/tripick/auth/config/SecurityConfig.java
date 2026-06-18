package com.tripick.auth.config;

import com.tripick.common.security.HeaderAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * [auth-service] Spring Security 설정
 * 공개 경로: 로그인·회원가입·비밀번호 재설정 등 인증 불필요 엔드포인트
 * 보호 경로: 로그아웃·비밀번호 변경 등 (gateway에서 JWT 검증 후 X-User-Id 헤더 전달)
 * HeaderAuthenticationFilter → X-User-Id 헤더를 SecurityContext에 등록
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final HeaderAuthenticationFilter headerAuthenticationFilter;

    private static final String[] PUBLIC_URLS = {
        "/api/auth/register",
        "/api/auth/login",
        "/api/auth/check-email",
        "/api/auth/token/refresh",
        "/api/auth/password/forgot",
        "/api/auth/password/reset",
        "/swagger-ui/**",
        "/api-docs/**",
        "/h2-console/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // JWT 사용 → 세션 없음
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_URLS).permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers ->
                headers.frameOptions(frame -> frame.sameOrigin())  // H2 콘솔 iframe 허용
            )
            // UsernamePasswordAuthenticationFilter 전에 헤더 기반 인증 필터 실행
            .addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
