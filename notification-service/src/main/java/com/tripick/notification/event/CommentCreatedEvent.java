package com.tripick.notification.event;

/**
 * community-service의 com.tripick.community.event.CommentCreatedEvent와 동일한 필드 구조.
 * JsonDeserializer가 com.tripick.* trusted package 내에서 타입명만으로 역직렬화하므로 필드 순서/이름을 맞춰야 함
 */
public record CommentCreatedEvent(Long postId, Long postOwnerId, Long commenterId, Long commentId) {
}
