package com.tripick.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripick.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * [gateway-service] JWT 인증 게이트웨이 필터 (WebFlux GlobalFilter)
 * 모든 요청의 진입점 - 인증 성공 시 X-User-Id/X-User-Role 헤더를 추가해 downstream으로 전달
 * 실패 시 각 서비스까지 요청이 전달되지 않고 여기서 즉시 401 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    // 토큰 없이 접근 가능한 공개 경로 목록
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/check-email",
            "/api/auth/password/forgot",
            "/api/auth/password/reset",
            "/api/auth/token/refresh"   // 리프레시 토큰은 auth-service에서 자체 검증
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        HttpMethod method = request.getMethod();

        // 공개 경로 → 검증 없이 downstream 전달
        if (isPublicPath(path, method)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다.");
        }

        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            // 만료 vs 위조 구분해서 다른 에러 코드 반환
            if (jwtTokenProvider.isExpired(token)) {
                return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "토큰이 만료되었습니다.");
            }
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다.");
        }

        Long userId = jwtTokenProvider.getUserId(token);
        String role = jwtTokenProvider.getRole(token);

        log.debug("[Gateway] 인증 성공 userId={}, role={}, path={}", userId, role, path);

        // 검증된 userId/role을 헤더로 변환 → downstream 서비스에서 HeaderAuthenticationFilter가 읽음
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Role", role != null ? role : "USER")
                        .header("Authorization", "")  // downstream에서 원본 JWT 재사용 방지
                )
                .build();

        return chain.filter(mutatedExchange);
    }

    private boolean isPublicPath(String path, HttpMethod method) {
        // 축제 조회는 비로그인 허용 (GET만)
        if (HttpMethod.GET.equals(method) && path.startsWith("/api/festivals")) {
            return true;
        }
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    // WebFlux는 서블릿 방식 response.sendError 불가 → 직접 JSON 바이트로 응답 작성
    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status,
                                          String code, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "success", false,
                "error", Map.of("code", code, "message", message)
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1; // 다른 GlobalFilter보다 먼저 실행 (라우팅 전 인증 처리)
    }
}
