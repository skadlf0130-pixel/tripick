package com.tripick.community.entity;

import com.tripick.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * [community-service] 게시물/댓글 신고. status 변경은 관리자만 수행 (ReportService.process)
 */
@Entity
@Table(name = "reports",
       uniqueConstraints = @UniqueConstraint(columnNames = {"target_type", "target_id", "reporter_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public void accept() {
        this.status = Status.ACCEPTED;
        this.processedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = Status.REJECTED;
        this.processedAt = LocalDateTime.now();
    }

    public enum TargetType {
        POST, COMMENT
    }

    public enum Status {
        PENDING, ACCEPTED, REJECTED
    }
}
