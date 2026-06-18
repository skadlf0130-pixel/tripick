package com.tripick.recommendation.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * [recommendation-service] 추천 결과 ↔ 여행지 연결 엔티티
 * MSA 경계: TravelSpot은 festival-service 소유 → spotId(Long)만 보관, JPA 관계 없음
 */
@Entity
@Table(name = "recommendation_spots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RecommendationSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private Recommendation recommendation;

    // festival-service 소유 - 직접 조인 불가, spotId로만 참조
    @Column(name = "spot_id", nullable = false)
    private Long spotId;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
