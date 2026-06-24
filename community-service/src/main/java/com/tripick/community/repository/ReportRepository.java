package com.tripick.community.repository;

import com.tripick.community.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByTargetTypeAndTargetIdAndReporterId(
            Report.TargetType targetType, Long targetId, Long reporterId);

    Page<Report> findByStatus(Report.Status status, Pageable pageable);
}
