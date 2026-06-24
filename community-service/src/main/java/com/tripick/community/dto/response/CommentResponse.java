package com.tripick.community.dto.response;

import com.tripick.community.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponse {

    private final Long commentId;
    private final Long postId;
    private final Long userId;
    private final String content;
    private final LocalDateTime createdAt;

    public CommentResponse(Comment comment) {
        this.commentId = comment.getId();
        this.postId = comment.getPost().getId();
        this.userId = comment.getUserId();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
    }
}
