package com.tripick.notification.entity;

/**
 * [notification-service] 알림 유형별 수신 채널. PUSH는 실제 푸시 발송 연동 전이라 인앱 알림센터 기록만 남김
 */
public enum NotificationChannel {
    PUSH,
    EMAIL,
    OFF
}
