package com.tripick.festival.controller;

import com.tripick.common.dto.response.ApiResponse;
import com.tripick.festival.dto.response.FestivalDetailResponse;
import com.tripick.festival.dto.response.FestivalResponse;
import com.tripick.festival.entity.Festival;
import com.tripick.festival.service.FestivalService;
import com.tripick.festival.service.FestivalSyncService;
import com.tripick.festival.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/festivals")
@RequiredArgsConstructor
@Tag(name = "Festival", description = "축제 API")
public class FestivalController {

    private final FestivalService festivalService;
    private final FestivalSyncService festivalSyncService;
    private final WeatherService weatherService;

    @GetMapping
    @Operation(summary = "축제 목록 조회", description = "월별/지역별/카테고리별 축제 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<FestivalResponse>>> getFestivals(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<FestivalResponse> result = festivalService.getFestivals(region, month, category, PageRequest.of(page, size))
                .map(FestivalResponse::new);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "축제 상세 조회", description = "축제 기간이 기상청 중기예보 제공 범위(오늘+3일~+10일)와 겹치면 날씨 예보를 함께 반환합니다.")
    public ResponseEntity<ApiResponse<FestivalDetailResponse>> getFestival(@PathVariable Long id) {
        Festival festival = festivalService.getFestival(id);
        FestivalDetailResponse response = new FestivalDetailResponse(festival, weatherService.getForecast(festival));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[관리자] 전국문화축제표준데이터 수동 동기화")
    public ResponseEntity<ApiResponse<Void>> syncFestivals() {
        int synced = festivalSyncService.syncAll();
        return ResponseEntity.ok(ApiResponse.ok(synced + "건 동기화되었습니다."));
    }
}
