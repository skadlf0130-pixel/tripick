package com.tripick.community.event;

public record CommentCreatedEvent(Long postId, Long postOwnerId, Long commenterId, Long commentId) {
}
