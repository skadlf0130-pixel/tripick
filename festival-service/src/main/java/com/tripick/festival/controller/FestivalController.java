package com.tripick.festival.controller;

import com.tripick.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/festivals")
@RequiredArgsConstructor
@Tag(name = "Festival", description = "축제 API")
public class FestivalController {

    // TODO: FestivalService, ReviewService 주입

    @GetMapping
    @Operation(summary = "축제 목록 조회", description = "월별/지역별 축제 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<?>> getFestivals(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var data = Map.of(
            "content", List.of(
                Map.of(
                    "festivalId", 1L,
                    "name", "서울 봄꽃 축제",
                    "startDate", "2026-04-10",
                    "endDate", "2026-04-20",
                    "region", "서울",
                    "category", "자연",
                    "imageUrl", "https://example.com/image.jpg"
                )
            ),
            "pageNumber", page,
            "pageSize", size,
            "totalPages", 1,
            "totalElements", 1,
            "isLast", true
        );
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/{id}")
    @Operation(summary = "축제 상세 조회")
    public ResponseEntity<ApiResponse<?>> getFestival(@PathVariable Long id) {
        var data = Map.ofEntries(
            Map.entry("festivalId", id),
            Map.entry("name", "서울 봄꽃 축제"),
            Map.entry("startDate", "2026-04-10"),
            Map.entry("endDate", "2026-04-20"),
            Map.entry("region", "서울"),
            Map.entry("category", "자연"),
            Map.entry("description", "한강변에서 펼쳐지는 봄꽃 축제입니다."),
            Map.entry("imageUrl", "https://example.com/image.jpg"),
            Map.entry("officialUrl", "https://example.com"),
            Map.entry("latitude", 37.5),
            Map.entry("longitude", 126.9)
        );
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/{id}/reviews")
    @Operation(summary = "축제 후기 목록 조회")
    public ResponseEntity<ApiResponse<?>> getReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "content", List.of(),
            "averageRating", 0.0,
            "totalPages", 0,
            "totalElements", 0
        )));
    }
}