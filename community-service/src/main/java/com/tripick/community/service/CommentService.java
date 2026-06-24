package com.tripick.community.service;

import com.tripick.common.exception.ErrorCode;
import com.tripick.common.exception.TripickException;
import com.tripick.community.dto.request.CommentCreateRequest;
import com.tripick.community.entity.Comment;
import com.tripick.community.entity.Post;
import com.tripick.community.event.CommentCreatedEvent;
import com.tripick.community.event.CommunityEventPublisher;
import com.tripick.community.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final CommunityEventPublisher eventPublisher;

    @Transactional
    public Comment create(Long postId, Long userId, CommentCreateRequest request) {
        Post post = postService.findActivePost(postId);

        Comment comment = commentRepository.save(Comment.builder()
                .post(post)
                .userId(userId)
                .content(request.getContent())
                .build());

        post.increaseCommentCount();
        eventPublisher.publishCommentCreated(
                new CommentCreatedEvent(post.getId(), post.getUserId(), userId, comment.getId()));

        return comment;
    }

    public Page<Comment> getPage(Long postId, Pageable pageable) {
        return commentRepository.findByPostIdAndIsDeletedFalse(postId, pageable);
    }

    @Transactional
    public void delete(Long postId, Long commentId, Long userId) {
        Comment comment = commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new TripickException(ErrorCode.COMMENT_NOT_FOUND));
        if (!comment.isWriter(userId)) {
            throw new TripickException(ErrorCode.REVIEW_WRITER_MISMATCH);
        }
        comment.delete();
        comment.getPost().decreaseCommentCount();
    }
}
