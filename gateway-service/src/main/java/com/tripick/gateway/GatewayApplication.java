package com.tripick.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// common의 MVC 빈(HeaderAuthenticationFilter, GlobalExceptionHandler)을 스캔하지 않음
// Gateway는 WebFlux 전용 - JwtTokenProvider는 JwtConfig에서 직접 Bean으로 등록
@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
