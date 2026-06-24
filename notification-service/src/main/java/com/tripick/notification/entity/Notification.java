package com.tripick.notification.entity;

import com.tripick.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * [notification-service] 알림센터에 표시되는 알림 1건
 * MSA 경계: User는 auth-service 소유 → userId(Long)만 보관
 */
@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;

    public void markRead() {
        this.isRead = true;
    }

    public boolean isOwner(Long userId) {
        return this.userId.equals(userId);
    }
}
