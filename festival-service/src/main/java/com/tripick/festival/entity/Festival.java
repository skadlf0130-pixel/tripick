package com.tripick.festival.entity;

import com.tripick.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * [festival-service] 축제 엔티티
 * Tour API(한국관광공사)에서 데이터를 주기적으로 가져와 로컬 DB에 캐싱 (cachedAt 기준 24시간)
 * recommendation-service에서 축제 정보가 필요할 때 Feign으로 이 서비스 API를 호출
 */
@Entity
@Table(name = "festivals",
       indexes = {
           @Index(name = "idx_festival_region", columnList = "region"),
           @Index(name = "idx_festival_start_date", columnList = "start_date"),
           @Index(name = "idx_festival_api_id", columnList = "api_id", unique = true)
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Festival extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "festival_id")
    private Long id;

    @Column(name = "api_id", unique = true, length = 50)
    private String apiId;  // Tour API 콘텐츠 ID (중복 저장 방지 기준)

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(length = 50)
    private String region;

    @Column(length = 50)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "official_url", length = 500)
    private String officialUrl;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "cached_at")
    private LocalDateTime cachedAt;  // Tour API 마지막 동기화 시각

    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Bookmark> bookmarks = new ArrayList<>();

    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    public void updateCacheTime() {
        this.cachedAt = LocalDateTime.now();
    }

    // 24시간 초과 시 Tour API 재호출 필요 여부 판단
    public boolean isExpired() {
        if (this.cachedAt == null) return true;
        return this.cachedAt.isBefore(LocalDateTime.now().minusHours(24));
    }
}