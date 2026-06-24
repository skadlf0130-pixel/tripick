package com.tripick.community.service;

import com.tripick.common.exception.ErrorCode;
import com.tripick.common.exception.TripickException;
import com.tripick.community.entity.Comment;
import com.tripick.community.entity.Post;
import com.tripick.community.entity.Report;
import com.tripick.community.repository.CommentRepository;
import com.tripick.community.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final PostService postService;
    private final CommentRepository commentRepository;

    @Transactional
    public Report reportPost(Long postId, Long reporterId, String reason) {
        postService.findActivePost(postId);
        return create(Report.TargetType.POST, postId, reporterId, reason);
    }

    @Transactional
    public Report reportComment(Long commentId, Long reporterId, String reason) {
        commentRepository.findById(commentId)
                .orElseThrow(() -> new TripickException(ErrorCode.COMMENT_NOT_FOUND));
        return create(Report.TargetType.COMMENT, commentId, reporterId, reason);
    }

    private Report create(Report.TargetType targetType, Long targetId, Long reporterId, String reason) {
        if (reportRepository.existsByTargetTypeAndTargetIdAndReporterId(targetType, targetId, reporterId)) {
            throw new TripickException(ErrorCode.ALREADY_REPORTED);
        }
        return reportRepository.save(Report.builder()
                .targetType(targetType)
                .targetId(targetId)
                .reporterId(reporterId)
                .reason(reason)
                .build());
    }

    public Page<Report> getPendingReports(Pageable pageable) {
        return reportRepository.findByStatus(Report.Status.PENDING, pageable);
    }

    @Transactional
    public Report process(Long reportId, boolean accept) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new TripickException(ErrorCode.REPORT_NOT_FOUND));

        if (accept) {
            report.accept();
            hideTarget(report);
        } else {
            report.reject();
        }
        return report;
    }

    private void hideTarget(Report report) {
        if (report.getTargetType() == Report.TargetType.POST) {
            postService.findActivePost(report.getTargetId()).delete();
        } else {
            Comment comment = commentRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new TripickException(ErrorCode.COMMENT_NOT_FOUND));
            comment.delete();
        }
    }
}
