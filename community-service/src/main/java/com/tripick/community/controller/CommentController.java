package com.tripick.community.controller;

import com.tripick.common.dto.response.ApiResponse;
import com.tripick.community.dto.request.CommentCreateRequest;
import com.tripick.community.dto.response.CommentResponse;
import com.tripick.community.entity.Comment;
import com.tripick.community.service.CommentService;
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
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comment", description = "게시물 댓글 API")
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    @Operation(summary = "댓글 목록 조회")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<CommentResponse> result = commentService.getPage(postId, PageRequest.of(page, size)).map(CommentResponse::new);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping
    @Operation(summary = "댓글 작성")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        Comment comment = commentService.create(postId, userId, request);
        return ResponseEntity.ok(ApiResponse.ok(new CommentResponse(comment), "댓글이 등록되었습니다."));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "작성자 본인만 삭제 가능")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        commentService.delete(postId, commentId, userId);
        return ResponseEntity.ok(ApiResponse.ok("댓글이 삭제되었습니다."));
    }
}
