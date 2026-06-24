package com.tripick.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * [notification-service] 알림 서비스 - 포트 8085
 * 담당: 알림 센터(읽음/안읽음), 알림 유형별 수신 설정(푸시/이메일/끄기), 타 서비스 이벤트 구독 후 알림 발행
 * scanBasePackages에 common 포함 → HeaderAuthenticationFilter, GlobalExceptionHandler 자동 등록
 */
@SpringBootApplication(scanBasePackages = {"com.tripick.notification", "com.tripick.common"})
@EnableJpaAuditing
public class NotificationApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationApplication.class, args);
    }
}
