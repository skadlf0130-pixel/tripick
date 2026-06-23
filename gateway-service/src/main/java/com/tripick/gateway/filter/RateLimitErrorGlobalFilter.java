package com.tripick.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * [gateway-service] RequestRateLimiter가 반환하는 빈 429 응답을 공통 JSON 에러 포맷으로 변환
 * RedisRateLimiter는 초과 시 상태코드만 세팅하고 빈 바디로 setComplete()를 호출하므로,
 * ServerHttpResponseDecorator로 감싸 setComplete() 호출을 가로채 바디를 채워넣는다.
 */
@Component
@RequiredArgsConstructor
public class RateLimitErrorGlobalFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse originalResponse = exchange.getResponse();

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> setComplete() {
                if (HttpStatus.TOO_MANY_REQUESTS.equals(getStatusCode())) {
                    return writeRateLimitErrorBody(originalResponse);
                }
                return super.setComplete();
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    private Mono<Void> writeRateLimitErrorBody(ServerHttpResponse response) {
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "success", false,
                "error", Map.of("code", "RATE_LIMIT_EXCEEDED", "message", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.")
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -2; // JwtAuthenticationFilter(-1)보다 먼저 적용 - 응답 데코레이터를 먼저 씌워야 함
    }
}
