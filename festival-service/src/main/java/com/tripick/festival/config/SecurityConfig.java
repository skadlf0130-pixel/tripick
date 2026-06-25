package com.tripick.festival.config;

import com.tripick.common.security.HeaderAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * [festival-service] Spring Security 설정
 * 축제 조회(GET)는 비로그인 허용 - gateway에서도 동일하게 public 처리
 * 찜·후기 작성, 동기화(POST /api/festivals/sync) 등 변경 요청은 인증 필요 (X-User-Id 헤더 필수)
 * 관리자 동기화 트리거는 @PreAuthorize("hasRole('ADMIN')")로 제어 (EnableMethodSecurity 필요)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final HeaderAuthenticationFilter headerAuthenticationFilter;

    private static final String[] PUBLIC_URLS = {
        "/swagger-ui/**",
        "/api-docs/**",
        "/h2-console/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/festivals", "/api/festivals/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/travel-spots", "/api/travel-spots/**").permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers ->
                headers.frameOptions(frame -> frame.sameOrigin())
            )
            .addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
