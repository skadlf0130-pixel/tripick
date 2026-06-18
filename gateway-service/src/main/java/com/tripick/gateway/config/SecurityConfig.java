package com.tripick.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * [gateway-service] WebFlux Security 설정
 * Spring Security 자체는 모두 허용으로 설정 - 인증/인가는 JwtAuthenticationFilter(GlobalFilter)에서 처리
 * Gateway는 MVC가 아닌 WebFlux이므로 @EnableWebFluxSecurity + ServerHttpSecurity 사용
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange.anyExchange().permitAll())  // 인증은 GlobalFilter에 위임
                .build();
    }
}
