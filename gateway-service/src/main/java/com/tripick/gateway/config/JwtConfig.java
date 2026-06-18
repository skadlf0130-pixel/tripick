package com.tripick.gateway.config;

import com.tripick.common.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * [gateway-service] JwtTokenProvider Bean 등록
 * common의 JwtTokenProvider는 @Component가 아닌 순수 클래스이므로 여기서 직접 생성
 * gateway는 토큰 검증만 담당 (발급은 auth-service에서)
 */
@Configuration
public class JwtConfig {

    @Bean
    public JwtTokenProvider jwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {
        return new JwtTokenProvider(secret, accessExpiration, refreshExpiration);
    }
}
