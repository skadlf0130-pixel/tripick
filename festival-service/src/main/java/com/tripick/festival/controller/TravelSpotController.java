package com.tripick.festival.controller;

import com.tripick.common.dto.response.ApiResponse;
import com.tripick.festival.dto.request.TravelSpotCreateRequest;
import com.tripick.festival.dto.response.TravelSpotDetailResponse;
import com.tripick.festival.dto.response.TravelSpotResponse;
import com.tripick.festival.entity.TravelSpot;
import com.tripick.festival.service.TravelSpotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/travel-spots")
@RequiredArgsConstructor
@Tag(name = "TravelSpot", description = "여행지 API")
public class TravelSpotController {

    private final TravelSpotService travelSpotService;

    @GetMapping
    @Operation(summary = "여행지 목록 조회", description = "지역별/카테고리별 여행지 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<TravelSpotResponse>>> getTravelSpots(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<TravelSpotResponse> result = travelSpotService.getTravelSpots(region, category, PageRequest.of(page, size))
                .map(TravelSpotResponse::new);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "여행지 상세 조회")
    public ResponseEntity<ApiResponse<TravelSpotDetailResponse>> getTravelSpot(@PathVariable Long id) {
        TravelSpot spot = travelSpotService.getTravelSpot(id);
        return ResponseEntity.ok(ApiResponse.ok(new TravelSpotDetailResponse(spot)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[관리자] 여행지 등록")
    public ResponseEntity<ApiResponse<TravelSpotDetailResponse>> createTravelSpot(
            @Valid @RequestBody TravelSpotCreateRequest request
    ) {
        TravelSpot spot = travelSpotService.create(request);
        return ResponseEntity.ok(ApiResponse.ok(new TravelSpotDetailResponse(spot), "여행지가 등록되었습니다."));
    }
}
