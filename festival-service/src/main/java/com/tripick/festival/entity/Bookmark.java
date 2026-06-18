package com.tripick.festival.entity;

import com.tripick.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * [festival-service] 축제 찜 엔티티
 * MSA 경계: User는 auth-service 소유 → userId(Long)만 보관, JPA 관계 없음
 * 유니크 제약으로 동일 유저의 동일 축제 중복 찜 방지
 */
@Entity
@Table(name = "bookmarks",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "festival_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Bookmark extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long id;

    // auth-service 소유 - 직접 조인 불가, userId로만 참조
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;
}
