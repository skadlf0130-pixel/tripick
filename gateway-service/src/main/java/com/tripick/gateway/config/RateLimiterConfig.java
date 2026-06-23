package com.tripick.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * [gateway-service] RequestRateLimiter용 KeyResolver 설정
 * JwtAuthenticationFilter(order -1)가 인증 성공 시 X-User-Id 헤더를 주입하므로
 * 인증된 요청은 userId 기준, 비인증 요청(공개 경로)은 클라이언트 IP 기준으로 제한
 */
@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver userOrIpKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null) {
                return Mono.just("user:" + userId);
            }
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just("ip:" + ip);
        };
    }
}
