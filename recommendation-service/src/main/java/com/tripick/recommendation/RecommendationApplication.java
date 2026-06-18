package com.tripick.recommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * [recommendation-service] AI 추천 서비스 - 포트 8083
 * 담당: Claude API 호출, 추천 결과 캐싱(24h), 이력 관리
 * @EnableFeignClients: festival-service 동기 호출(FestivalClient) 활성화
 * scanBasePackages에 common 포함 → HeaderAuthenticationFilter, GlobalExceptionHandler 자동 등록
 */
@SpringBootApplication(scanBasePackages = {"com.tripick.recommendation", "com.tripick.common"})
@EnableFeignClients
@EnableJpaAuditing
public class RecommendationApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecommendationApplication.class, args);
    }
}
