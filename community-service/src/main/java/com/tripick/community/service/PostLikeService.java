package com.tripick.community.service;

import com.tripick.common.exception.ErrorCode;
import com.tripick.common.exception.TripickException;
import com.tripick.community.entity.Post;
import com.tripick.community.entity.PostLike;
import com.tripick.community.event.CommunityEventPublisher;
import com.tripick.community.event.PostLikedEvent;
import com.tripick.community.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostService postService;
    private final CommunityEventPublisher eventPublisher;

    @Transactional
    public void like(Long postId, Long userId) {
        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new TripickException(ErrorCode.ALREADY_LIKED);
        }
        Post post = postService.findActivePost(postId);

        postLikeRepository.save(PostLike.builder()
                .post(post)
                .userId(userId)
                .build());
        post.increaseLikeCount();

        eventPublisher.publishPostLiked(new PostLikedEvent(post.getId(), post.getUserId(), userId));
    }

    @Transactional
    public void unlike(Long postId, Long userId) {
        postLikeRepository.findByPostIdAndUserId(postId, userId)
                .ifPresent(postLike -> {
                    postLikeRepository.delete(postLike);
                    postService.findActivePost(postId).decreaseLikeCount();
                });
    }
}
