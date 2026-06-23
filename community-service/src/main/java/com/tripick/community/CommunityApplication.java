package com.tripick.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * [community-service] 커뮤니티 서비스 - 포트 8084
 * 담당: 여행 후기(텍스트/사진/동영상) 작성, 댓글/좋아요, 신고 및 관리
 * scanBasePackages에 common 포함 → HeaderAuthenticationFilter, GlobalExceptionHandler 자동 등록
 */
@SpringBootApplication(scanBasePackages = {"com.tripick.community", "com.tripick.common"})
@EnableJpaAuditing
public class CommunityApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }
}
