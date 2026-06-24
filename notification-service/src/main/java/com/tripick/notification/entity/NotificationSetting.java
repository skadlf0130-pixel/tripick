package com.tripick.notification.entity;

import com.tripick.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * [notification-service] 사용자별 알림 유형별 수신 채널 설정. 행이 없으면 기본값 PUSH로 간주(NotificationSettingService 참고)
 */
@Entity
@Table(name = "notification_settings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "type"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NotificationSetting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_setting_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NotificationChannel channel;

    // channel=EMAIL일 때만 사용. auth-service에 사용자 조회 API가 없어 발송 대상 이메일을 직접 등록받음
    @Column(length = 100)
    private String email;

    public void change(NotificationChannel channel, String email) {
        this.channel = channel;
        this.email = email;
    }
}
