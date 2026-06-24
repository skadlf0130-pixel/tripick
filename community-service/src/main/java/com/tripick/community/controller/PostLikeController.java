package com.tripick.community.controller;

import com.tripick.common.dto.response.ApiResponse;
import com.tripick.community.service.PostLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
@RequiredArgsConstructor
@Tag(name = "PostLike", description = "게시물 좋아요 API")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping
    @Operation(summary = "좋아요")
    public ResponseEntity<ApiResponse<Void>> like(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {
        postLikeService.like(postId, userId);
        return ResponseEntity.ok(ApiResponse.ok("좋아요를 눌렀습니다."));
    }

    @DeleteMapping
    @Operation(summary = "좋아요 취소")
    public ResponseEntity<ApiResponse<Void>> unlike(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {
        postLikeService.unlike(postId, userId);
        return ResponseEntity.ok(ApiResponse.ok("좋아요를 취소했습니다."));
    }
}
