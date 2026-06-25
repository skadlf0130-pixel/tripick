package com.tripick.festival.controller;

import com.tripick.common.dto.response.ApiResponse;
import com.tripick.festival.dto.request.ReviewRequest;
import com.tripick.festival.dto.response.ReviewPageResponse;
import com.tripick.festival.dto.response.ReviewResponse;
import com.tripick.festival.entity.Review;
import com.tripick.festival.service.ReviewService;
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
@RequestMapping("/api/festivals/{festivalId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "축제 후기 API")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    @Operation(summary = "축제 후기 목록 조회")
    public ResponseEntity<ApiResponse<ReviewPageResponse>> getReviews(
            @PathVariable Long festivalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ReviewResponse> result = reviewService.getReviews(festivalId, PageRequest.of(page, size)).map(ReviewResponse::new);
        double averageRating = reviewService.getAverageRating(festivalId);
        return ResponseEntity.ok(ApiResponse.ok(new ReviewPageResponse(result, averageRating)));
    }

    @PostMapping
    @Operation(summary = "축제 후기 작성", description = "축제당 1인 1후기만 작성 가능")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long festivalId,
            @Valid @RequestBody ReviewRequest request
    ) {
        Review review = reviewService.create(festivalId, userId, request);
        return ResponseEntity.ok(ApiResponse.ok(new ReviewResponse(review), "후기가 등록되었습니다."));
    }

    @PatchMapping("/{reviewId}")
    @Operation(summary = "축제 후기 수정", description = "작성자 본인만 수정 가능")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long festivalId,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request
    ) {
        Review review = reviewService.update(festivalId, reviewId, userId, request);
        return ResponseEntity.ok(ApiResponse.ok(new ReviewResponse(review), "후기가 수정되었습니다."));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "축제 후기 삭제", description = "작성자 본인만 삭제 가능")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long festivalId,
            @PathVariable Long reviewId
    ) {
        reviewService.delete(festivalId, reviewId, userId);
        return ResponseEntity.ok(ApiResponse.ok("후기가 삭제되었습니다."));
    }
}
