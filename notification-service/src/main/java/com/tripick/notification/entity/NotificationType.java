package com.tripick.notification.entity;

/**
 * [notification-service] 알림 유형. community-service가 발행하는 이벤트 종류와 1:1 대응
 * 새 이벤트가 추가되면(예: 추후 festival.updated) 여기에 enum 값과 listener를 함께 추가
 */
public enum NotificationType {
    COMMENT_CREATED,
    POST_LIKED
}
