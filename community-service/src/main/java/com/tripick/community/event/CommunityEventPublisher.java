package com.tripick.community.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * [community-service] notification-service가 구독하는 이벤트 발행
 * 수신자(postOwnerId)를 발행 시점에 결정해서 payload에 포함 → consumer는 cross-service 조회 없이 그대로 알림 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityEventPublisher {

    private static final String TOPIC_COMMENT_CREATED = "comment.created";
    private static final String TOPIC_POST_LIKED = "post.liked";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCommentCreated(CommentCreatedEvent event) {
        if (event.postOwnerId().equals(event.commenterId())) {
            return;
        }
        send(TOPIC_COMMENT_CREATED, event.postId().toString(), event);
    }

    public void publishPostLiked(PostLikedEvent event) {
        if (event.postOwnerId().equals(event.likerId())) {
            return;
        }
        send(TOPIC_POST_LIKED, event.postId().toString(), event);
    }

    // 알림 발행은 부가 기능이므로 Kafka 장애가 본 요청(댓글/좋아요)을 막지 않도록 예외를 흡수하고 비동기로만 처리
    private void send(String topic, String key, Object payload) {
        try {
            kafkaTemplate.send(topic, key, payload)
                    .exceptionally(ex -> {
                        log.warn("[CommunityEventPublisher] {} 발행 실패. key={}", topic, key, ex);
                        return null;
                    });
        } catch (Exception e) {
            log.warn("[CommunityEventPublisher] {} 발행 중 오류. key={}", topic, key, e);
        }
    }
}
