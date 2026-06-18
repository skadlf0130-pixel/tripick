package com.tripick.festival;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * [festival-service] 축제 서비스 - 포트 8082
 * 담당: 축제 조회(Tour API 캐싱), 여행지 관리, 후기, 찜
 * scanBasePackages에 common 포함 → HeaderAuthenticationFilter, GlobalExceptionHandler 자동 등록
 */
@SpringBootApplication(scanBasePackages = {"com.tripick.festival", "com.tripick.common"})
@EnableJpaAuditing
public class FestivalApplication {
    public static void main(String[] args) {
        SpringApplication.run(FestivalApplication.class, args);
    }
}
