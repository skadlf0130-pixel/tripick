package com.tripick.community.event;

public record PostLikedEvent(Long postId, Long postOwnerId, Long likerId) {
}
