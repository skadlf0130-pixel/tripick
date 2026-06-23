package com.tripick.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitErrorGlobalFilterTest {

    @Mock
    private GatewayFilterChain chain;

    private RateLimitErrorGlobalFilter filter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        filter = new RateLimitErrorGlobalFilter(objectMapper);
    }

    @Test
    @DisplayName("429 상태에서 setComplete() 호출 시 JSON 에러 바디로 변환")
    void filter_tooManyRequests_writesJsonErrorBody() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/festivals").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any())).thenAnswer(invocation -> {
            ServerWebExchange mutated = invocation.getArgument(0);
            mutated.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return mutated.getResponse().setComplete();
        });

        filter.filter(exchange, chain).block();

        String body = ((org.springframework.mock.http.server.reactive.MockServerHttpResponse) exchange.getResponse())
                .getBodyAsString().block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(body).contains("RATE_LIMIT_EXCEEDED");
        verify(chain).filter(any());
    }

    @Test
    @DisplayName("429가 아닌 상태에서는 원본 setComplete() 동작 유지")
    void filter_nonRateLimitStatus_doesNotModifyBody() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/festivals").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any())).thenAnswer(invocation -> {
            ServerWebExchange mutated = invocation.getArgument(0);
            mutated.getResponse().setStatusCode(HttpStatus.OK);
            return mutated.getResponse().setComplete();
        });

        filter.filter(exchange, chain).block();

        String body = ((org.springframework.mock.http.server.reactive.MockServerHttpResponse) exchange.getResponse())
                .getBodyAsString().block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body).isNullOrEmpty();
    }
}
