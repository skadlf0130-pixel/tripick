package com.tripick.notification.listener;

import com.tripick.notification.entity.NotificationType;
import com.tripick.notification.event.CommentCreatedEvent;
import com.tripick.notification.event.PostLikedEvent;
import com.tripick.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * [notification-service] community-service가 발행한 이벤트를 구독해 알림 생성
 * 수신자(postOwnerId)는 community-service가 이미 결정해서 보내므로 추가 조회 없이 그대로 사용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "comment.created", groupId = "notification-service")
    public void onCommentCreated(CommentCreatedEvent event) {
        log.debug("[CommentCreatedEvent 수신] postId={}, postOwnerId={}", event.postId(), event.postOwnerId());
        notificationService.createFromEvent(
                event.postOwnerId(),
                NotificationType.COMMENT_CREATED,
                "새 댓글이 달렸습니다",
                "내가 작성한 게시물에 새 댓글이 달렸습니다.",
                event.postId()
        );
    }

    @KafkaListener(topics = "post.liked", groupId = "notification-service")
    public void onPostLiked(PostLikedEvent event) {
        log.debug("[PostLikedEvent 수신] postId={}, postOwnerId={}", event.postId(), event.postOwnerId());
        notificationService.createFromEvent(
                event.postOwnerId(),
                NotificationType.POST_LIKED,
                "좋아요를 받았습니다",
                "내가 작성한 게시물에 좋아요가 달렸습니다.",
                event.postId()
        );
    }
}
