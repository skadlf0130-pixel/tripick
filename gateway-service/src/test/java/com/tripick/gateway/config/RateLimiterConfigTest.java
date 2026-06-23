package com.tripick.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterConfigTest {

    private final KeyResolver keyResolver = new RateLimiterConfig().userOrIpKeyResolver();

    @Test
    void resolve_withUserIdHeader_returnsUserKey() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/festivals")
                .header("X-User-Id", "42")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        String key = keyResolver.resolve(exchange).block();

        assertThat(key).isEqualTo("user:42");
    }

    @Test
    void resolve_withoutUserIdHeader_returnsIpKey() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/festivals").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        String key = keyResolver.resolve(exchange).block();

        assertThat(key).startsWith("ip:");
    }
}
