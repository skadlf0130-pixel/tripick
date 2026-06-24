package com.tripick.community.service;

import com.tripick.common.exception.ErrorCode;
import com.tripick.common.exception.TripickException;
import com.tripick.community.dto.request.PostCreateRequest;
import com.tripick.community.entity.Post;
import com.tripick.community.entity.PostMedia;
import com.tripick.community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private static final int MAX_PHOTOS = 5;

    private final PostRepository postRepository;

    @Transactional
    public Post create(Long userId, PostCreateRequest request) {
        List<String> photoUrls = request.getPhotoUrls() != null ? request.getPhotoUrls() : List.of();
        if (photoUrls.size() > MAX_PHOTOS) {
            throw new TripickException(ErrorCode.TOO_MANY_PHOTOS);
        }

        Post post = Post.builder()
                .userId(userId)
                .festivalId(request.getFestivalId())
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        int sortOrder = 0;
        for (String photoUrl : photoUrls) {
            post.addMedia(PostMedia.builder()
                    .mediaType(PostMedia.MediaType.PHOTO)
                    .url(photoUrl)
                    .sortOrder(sortOrder++)
                    .build());
        }
        if (request.getVideoUrl() != null) {
            post.addMedia(PostMedia.builder()
                    .mediaType(PostMedia.MediaType.VIDEO)
                    .url(request.getVideoUrl())
                    .sortOrder(sortOrder)
                    .build());
        }

        return postRepository.save(post);
    }

    public Page<Post> getPage(Pageable pageable) {
        return postRepository.findByIsDeletedFalse(pageable);
    }

    @Transactional
    public Post getDetail(Long postId) {
        Post post = postRepository.findDetailByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new TripickException(ErrorCode.REVIEW_NOT_FOUND));
        post.increaseViewCount();
        return post;
    }

    @Transactional
    public void delete(Long postId, Long userId) {
        Post post = findActivePost(postId);
        if (!post.isWriter(userId)) {
            throw new TripickException(ErrorCode.REVIEW_WRITER_MISMATCH);
        }
        post.delete();
    }

    public Post findActivePost(Long postId) {
        return postRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new TripickException(ErrorCode.REVIEW_NOT_FOUND));
    }
}
