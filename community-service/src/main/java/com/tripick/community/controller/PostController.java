package com.tripick.community.controller;

import com.tripick.common.dto.response.ApiResponse;
import com.tripick.community.dto.request.PostCreateRequest;
import com.tripick.community.dto.response.PostDetailResponse;
import com.tripick.community.dto.response.PostResponse;
import com.tripick.community.entity.Post;
import com.tripick.community.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Post", description = "커뮤니티 후기 게시물 API")
public class PostController {

    private final PostService postService;

    @GetMapping
    @Operation(summary = "후기 게시물 목록 조회")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<PostResponse> result = postService.getPage(PageRequest.of(page, size)).map(PostResponse::new);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "후기 게시물 상세 조회")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(@PathVariable Long postId) {
        Post post = postService.getDetail(postId);
        return ResponseEntity.ok(ApiResponse.ok(new PostDetailResponse(post)));
    }

    @PostMapping
    @Operation(summary = "후기 게시물 작성", description = "사진 최대 5장, 영상 최대 1개까지 첨부 가능")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PostCreateRequest request
    ) {
        Post post = postService.create(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(new PostResponse(post), "게시물이 등록되었습니다."));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "후기 게시물 삭제", description = "작성자 본인만 삭제 가능")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {
        postService.delete(postId, userId);
        return ResponseEntity.ok(ApiResponse.ok("게시물이 삭제되었습니다."));
    }
}
