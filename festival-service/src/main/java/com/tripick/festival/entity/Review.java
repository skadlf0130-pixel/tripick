package com.tripick.festival.entity;

import com.tripick.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * [festival-service] 축제 후기 엔티티
 * MSA 경계: User는 auth-service 소유 → userId(Long)만 보관
 * 후기 작성 시 Kafka(review.created) 이벤트 발행 → 평점 업데이트 등 비동기 처리
 */
@Entity
@Table(name = "reviews",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "festival_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    // auth-service 소유 - 직접 조인 불가, userId로만 참조
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @Column(nullable = false)
    private Integer rating;  // 1 ~ 5

    @Column(length = 500)
    private String content;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    public void delete() {
        this.isDeleted = true;
    }

    public void update(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
    }

    // userId를 직접 비교 (User 엔티티 참조 없이 작성자 확인)
    public boolean isWriter(Long userId) {
        return this.userId.equals(userId);
    }
}
