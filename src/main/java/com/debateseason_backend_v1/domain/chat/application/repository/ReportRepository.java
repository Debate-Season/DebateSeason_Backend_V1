package com.debateseason_backend_v1.domain.chat.application.repository;

import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportStatus;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportTargetType;
import com.debateseason_backend_v1.domain.chat.infrastructure.report.ReportEntity;

import java.util.List;

public interface ReportRepository {

    boolean existByTargetIdAndReportIdAndReportType(Long targetId, Long reportId, ReportTargetType reportType);

    ReportEntity save(ReportEntity reportEntity);

    ReportEntity findById(Long reportId);
    
    List<ReportEntity> findByTargetTypeAndStatus(ReportTargetType targetType, ReportStatus status);
}
