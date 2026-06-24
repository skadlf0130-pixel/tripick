package com.tripick.notification.event;

/** community-service의 com.tripick.community.event.PostLikedEvent와 동일한 필드 구조 */
public record PostLikedEvent(Long postId, Long postOwnerId, Long likerId) {
}
