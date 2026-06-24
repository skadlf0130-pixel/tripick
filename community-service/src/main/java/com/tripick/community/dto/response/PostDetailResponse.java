package com.tripick.community.dto.response;

import com.tripick.community.entity.Post;
import com.tripick.community.entity.PostMedia;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostDetailResponse {

    private final Long postId;
    private final Long userId;
    private final Long festivalId;
    private final String title;
    private final String content;
    private final long viewCount;
    private final long likeCount;
    private final long commentCount;
    private final List<MediaItem> media;
    private final LocalDateTime createdAt;

    public PostDetailResponse(Post post) {
        this.postId = post.getId();
        this.userId = post.getUserId();
        this.festivalId = post.getFestivalId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.createdAt = post.getCreatedAt();
        this.media = post.getMedia().stream().map(MediaItem::new).toList();
    }

    @Getter
    public static class MediaItem {
        private final String mediaType;
        private final String url;
        private final int sortOrder;

        public MediaItem(PostMedia postMedia) {
            this.mediaType = postMedia.getMediaType().name();
            this.url = postMedia.getUrl();
            this.sortOrder = postMedia.getSortOrder();
        }
    }
}
