package com.tripick.recommendation.controller;

import com.tripick.common.dto.response.ApiResponse;
import com.tripick.recommendation.dto.request.RecommendationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendation", description = "AI 추천 API")
public class RecommendationController {

    // TODO: RecommendationService 주입

    @PostMapping
    @Operation(summary = "AI 추천 요청", description = "취향 정보를 입력받아 AI가 축제와 여행지를 추천합니다.")
    public ResponseEntity<ApiResponse<?>> recommend(@Valid @RequestBody RecommendationRequest request) {
        var data = Map.of(
            "recommendationId", 1L,
            "festivals", List.of(
                Map.of(
                    "festivalId", 1L,
                    "name", "서울 봄꽃 축제",
                    "startDate", "2026-04-10",
                    "endDate", "2026-04-20",
                    "region", "서울",
                    "category", "자연",
                    "aiComment", "대중교통 10분 거리! 봄꽃과 먹거리가 함께해요",
                    "sortOrder", 1
                )
            ),
            "travelSpots", List.of(
                Map.of("spotId", 1L, "name", "경복궁", "region", "서울", "category", "문화")
            ),
            "createdAt", "2026-04-27T10:00:00"
        );
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/{id}")
    @Operation(summary = "추천 결과 조회")
    public ResponseEntity<ApiResponse<?>> getRecommendation(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("recommendationId", id)));
    }
}