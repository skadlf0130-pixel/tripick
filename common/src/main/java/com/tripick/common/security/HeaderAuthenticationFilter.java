package com.tripick.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * [common] 게이트웨이 통과 후 헤더 기반 인증 필터
 * 흐름: gateway(JWT 검증) → X-User-Id / X-User-Role 헤더 추가 → 각 서비스 이 필터에서 SecurityContext 세팅
 * JWT를 각 서비스에서 재검증하지 않으므로 성능 절약, 인증 로직 게이트웨이에 집중
 * auth-service, festival-service, recommendation-service 공통 사용
 */
@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userIdHeader = request.getHeader("X-User-Id");
        String roleHeader = request.getHeader("X-User-Role");

        // 헤더 있을 때만 인증 처리 (없으면 anonymous로 통과 → SecurityConfig에서 접근 제어)
        if (userIdHeader != null) {
            Long userId = Long.parseLong(userIdHeader);
            String role = roleHeader != null ? roleHeader : "USER";

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
