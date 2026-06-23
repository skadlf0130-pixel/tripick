package com.tripick.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripick.common.security.JwtTokenProvider;
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
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private GatewayFilterChain chain;

    private JwtAuthenticationFilter filter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider, objectMapper);
    }

    @Test
    @DisplayName("POST /api/auth/login - 공개 경로는 토큰 검증 없이 통과")
    void filter_publicPath_login_passesThroughWithoutValidation() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/auth/login").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(jwtTokenProvider, never()).validateToken(any());
        verify(chain).filter(any());
    }

    @Test
    @DisplayName("POST /api/auth/register - 공개 경로 통과")
    void filter_publicPath_register_passesThroughWithoutValidation() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/auth/register").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(jwtTokenProvider, never()).validateToken(any());
        verify(chain).filter(any());
    }

    @Test
    @DisplayName("GET /api/festivals - 축제 조회 공개 경로 통과")
    void filter_publicPath_getFestivals_passesThroughWithoutValidation() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/festivals").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(jwtTokenProvider, never()).validateToken(any());
        verify(chain).filter(any());
    }

    @Test
    @DisplayName("Authorization 헤더 없음 - 401 반환")
    void filter_missingAuthHeader_returnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/recommendations").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("Bearer 형식 아닌 헤더 - 401 반환")
    void filter_invalidAuthHeaderFormat_returnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/recommendations")
                .header("Authorization", "Basic sometoken")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("유효하지 않은 토큰 - 401 반환")
    void filter_invalidToken_returnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/recommendations")
                .header("Authorization", "Bearer invalid.token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtTokenProvider.validateToken("invalid.token")).thenReturn(false);
        when(jwtTokenProvider.isExpired("invalid.token")).thenReturn(false);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("만료된 토큰 - 401 반환")
    void filter_expiredToken_returnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/recommendations")
                .header("Authorization", "Bearer expired.token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtTokenProvider.validateToken("expired.token")).thenReturn(false);
        when(jwtTokenProvider.isExpired("expired.token")).thenReturn(true);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("유효한 토큰 - X-User-Id 헤더 추가 후 통과")
    void filter_validToken_addsUserIdHeaderAndPassesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/recommendations")
                .header("Authorization", "Bearer " + VALID_TOKEN)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUserId(VALID_TOKEN)).thenReturn(USER_ID);
        when(jwtTokenProvider.getRole(VALID_TOKEN)).thenReturn("USER");
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
        verify(jwtTokenProvider).getUserId(VALID_TOKEN);
    }

    @Test
    @DisplayName("필터 우선순위가 -1 인지 확인")
    void filter_order_isMinusOne() {
        assertThat(filter.getOrder()).isEqualTo(-1);
    }
}