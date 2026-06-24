package com.tripick.community.dto.response;

import com.tripick.community.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostResponse {

    private final Long postId;
    private final Long userId;
    private final Long festivalId;
    private final String title;
    private final String content;
    private final long viewCount;
    private final long likeCount;
    private final long commentCount;
    private final LocalDateTime createdAt;

    public PostResponse(Post post) {
        this.postId = post.getId();
        this.userId = post.getUserId();
        this.festivalId = post.getFestivalId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.createdAt = post.getCreatedAt();
    }
}
