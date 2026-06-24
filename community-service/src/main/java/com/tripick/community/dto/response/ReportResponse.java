package com.tripick.community.dto.response;

import com.tripick.community.entity.Report;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReportResponse {

    private final Long reportId;
    private final String targetType;
    private final Long targetId;
    private final Long reporterId;
    private final String reason;
    private final String status;
    private final LocalDateTime createdAt;

    public ReportResponse(Report report) {
        this.reportId = report.getId();
        this.targetType = report.getTargetType().name();
        this.targetId = report.getTargetId();
        this.reporterId = report.getReporterId();
        this.reason = report.getReason();
        this.status = report.getStatus().name();
        this.createdAt = report.getCreatedAt();
    }
}
