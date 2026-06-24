package com.tripick.community.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * [community-service] 게시물 첨부 미디어 (사진 최대 5장 + 영상 최대 1개, 개수 검증은 PostService에서 수행)
 */
@Entity
@Table(name = "post_media")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_media_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MediaType mediaType;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    void setPost(Post post) {
        this.post = post;
    }

    public enum MediaType {
        PHOTO, VIDEO
    }
}
