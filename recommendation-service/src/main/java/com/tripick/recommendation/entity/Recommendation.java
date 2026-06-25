package com.tripick.recommendation.entity;

import com.tripick.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * [recommendation-service] AI 추천 결과 엔티티
 * Claude API 호출 결과를 resultJson에 통째로 캐싱 (24시간) → 동일 조건 재요청 시 API 재호출 없이 반환
 * MSA 경계: User는 auth-service 소유 → userId(Long)만 보관
 */
@Entity
@Table(name = "recommendations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Recommendation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Long id;

    // auth-service 소유 - 직접 조인 불가, userId로만 참조
    @Column(name = "user_id")
    private Long userId;

    @Column(length = 50)
    private String region;

    @Column(columnDefinition = "JSON")
    private String interests;      // ["자연", "먹거리"] JSON 배열 문자열

    @Column(length = 20)
    private String transportation;

    // region+interests+transportation 조합 캐시 키 (동일 조건 재요청 시 재사용)
    @Column(name = "condition_key", length = 255)
    private String conditionKey;

    @Column(name = "result_json", columnDefinition = "JSON")
    private String resultJson;     // Claude API 응답 전체 캐싱

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @OneToMany(mappedBy = "recommendation", cascade = CascadeType.ALL)
    @Builder.Default
    private List<RecommendationFestival> recommendationFestivals = new ArrayList<>();

    @OneToMany(mappedBy = "recommendation", cascade = CascadeType.ALL)
    @Builder.Default
    private List<RecommendationSpot> recommendationSpots = new ArrayList<>();

    public boolean isExpired() {
        return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
    }

    public static LocalDateTime defaultExpiresAt() {
        return LocalDateTime.now().plusHours(24);
    }
}
