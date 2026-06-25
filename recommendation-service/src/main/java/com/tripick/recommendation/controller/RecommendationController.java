package com.tripick.recommendation.controller;

import com.tripick.common.dto.response.ApiResponse;
import com.tripick.recommendation.dto.request.RecommendationRequest;
import com.tripick.recommendation.dto.response.RecommendationResponse;
import com.tripick.recommendation.entity.Recommendation;
import com.tripick.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendation", description = "AI 추천 API")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping
    @Operation(summary = "AI 추천 요청", description = "취향 정보를 입력받아 AI가 축제와 여행지를 추천합니다. 동일 조건 재요청 시 24시간 내 결과를 재사용합니다.")
    public ResponseEntity<ApiResponse<RecommendationResponse>> recommend(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RecommendationRequest request
    ) {
        Recommendation recommendation = recommendationService.recommend(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(recommendationService.toResponse(recommendation)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "추천 결과 조회")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getRecommendation(@PathVariable Long id) {
        Recommendation recommendation = recommendationService.getRecommendation(id);
        return ResponseEntity.ok(ApiResponse.ok(recommendationService.toResponse(recommendation)));
    }
}
