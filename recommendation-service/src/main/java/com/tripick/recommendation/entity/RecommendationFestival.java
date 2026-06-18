package com.tripick.recommendation.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * [recommendation-service] 추천 결과 ↔ 축제 연결 엔티티
 * MSA 경계: Festival은 festival-service 소유 → festivalId(Long)만 보관, JPA 관계 없음
 * 축제 상세 정보 필요 시 FestivalClient(Feign)로 festival-service 호출
 */
@Entity
@Table(name = "recommendation_festivals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RecommendationFestival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private Recommendation recommendation;

    // festival-service 소유 - 직접 조인 불가, festivalId로만 참조
    @Column(name = "festival_id", nullable = false)
    private Long festivalId;

    @Column(name = "ai_comment", length = 1000)
    private String aiComment;  // Claude가 생성한 이 축제 추천 이유

    @Column(name = "sort_order")
    private Integer sortOrder;  // 추천 우선순위
}
