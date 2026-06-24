package com.tripick.notification.controller;

import com.tripick.common.dto.response.ApiResponse;
import com.tripick.notification.dto.request.NotificationSettingUpdateRequest;
import com.tripick.notification.dto.response.NotificationSettingResponse;
import com.tripick.notification.entity.NotificationType;
import com.tripick.notification.service.NotificationSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications/settings")
@RequiredArgsConstructor
@Tag(name = "NotificationSetting", description = "알림 유형별 수신 설정 API")
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    @GetMapping
    @Operation(summary = "알림 유형별 수신 설정 조회", description = "설정하지 않은 유형은 기본값(PUSH)으로 표시")
    public ResponseEntity<ApiResponse<List<NotificationSettingResponse>>> getSettings(
            @AuthenticationPrincipal Long userId
    ) {
        List<NotificationSettingResponse> result = notificationSettingService.getAll(userId).stream()
                .map(s -> new NotificationSettingResponse(s.getType(), s.getChannel(), s.getEmail()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PutMapping("/{type}")
    @Operation(summary = "알림 유형별 수신 설정 변경", description = "channel=EMAIL 선택 시 email 필수")
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> updateSetting(
            @AuthenticationPrincipal Long userId,
            @PathVariable NotificationType type,
            @Valid @RequestBody NotificationSettingUpdateRequest request
    ) {
        var setting = notificationSettingService.update(userId, type, request.getChannel(), request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(
                new NotificationSettingResponse(setting.getType(), setting.getChannel(), setting.getEmail()),
                "수신 설정이 변경되었습니다."));
    }
}
