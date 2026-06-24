package com.tripick.community.controller;

import com.tripick.common.dto.response.ApiResponse;
import com.tripick.community.dto.request.ReportCreateRequest;
import com.tripick.community.dto.request.ReportDecisionRequest;
import com.tripick.community.dto.response.ReportResponse;
import com.tripick.community.entity.Report;
import com.tripick.community.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Report", description = "게시물/댓글 신고 및 관리자 처리 API")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/{postId}/report")
    @Operation(summary = "게시물 신고")
    public ResponseEntity<ApiResponse<ReportResponse>> reportPost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody ReportCreateRequest request
    ) {
        Report report = reportService.reportPost(postId, userId, request.getReason());
        return ResponseEntity.ok(ApiResponse.ok(new ReportResponse(report), "신고가 접수되었습니다."));
    }

    @PostMapping("/{postId}/comments/{commentId}/report")
    @Operation(summary = "댓글 신고")
    public ResponseEntity<ApiResponse<ReportResponse>> reportComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody ReportCreateRequest request
    ) {
        Report report = reportService.reportComment(commentId, userId, request.getReason());
        return ResponseEntity.ok(ApiResponse.ok(new ReportResponse(report), "신고가 접수되었습니다."));
    }

    @GetMapping("/admin/reports")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[관리자] 미처리 신고 목록 조회")
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getPendingReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ReportResponse> result = reportService.getPendingReports(PageRequest.of(page, size)).map(ReportResponse::new);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PatchMapping("/admin/reports/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[관리자] 신고 처리(승인/반려)", description = "승인 시 대상 게시물/댓글을 숨김 처리")
    public ResponseEntity<ApiResponse<ReportResponse>> processReport(
            @PathVariable Long reportId,
            @Valid @RequestBody ReportDecisionRequest request
    ) {
        Report report = reportService.process(reportId, request.getAccept());
        return ResponseEntity.ok(ApiResponse.ok(new ReportResponse(report), "신고가 처리되었습니다."));
    }
}
