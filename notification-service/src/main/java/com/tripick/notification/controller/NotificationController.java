package com.tripick.notification.controller;

import com.tripick.common.dto.response.ApiResponse;
import com.tripick.notification.dto.response.NotificationResponse;
import com.tripick.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림센터 API")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "내 알림 목록 조회")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<NotificationResponse> result = notificationService.getPage(userId, PageRequest.of(page, size))
                .map(NotificationResponse::new);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "읽지 않은 알림 개수 조회")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getUnreadCount(userId)));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "알림 읽음 처리")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long notificationId
    ) {
        notificationService.markRead(notificationId, userId);
        return ResponseEntity.ok(ApiResponse.ok("알림을 읽음으로 처리했습니다."));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "전체 알림 읽음 처리")
    public ResponseEntity<ApiResponse<Void>> markAllRead(@AuthenticationPrincipal Long userId) {
        notificationService.markAllRead(userId);
        return ResponseEntity.ok(ApiResponse.ok("모든 알림을 읽음으로 처리했습니다."));
    }
}
