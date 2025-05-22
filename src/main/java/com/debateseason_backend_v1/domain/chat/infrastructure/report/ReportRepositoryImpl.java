package com.debateseason_backend_v1.domain.chat.infrastructure.report;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.chat.application.repository.ReportRepository;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportStatus;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class ReportRepositoryImpl implements ReportRepository {

    private final ReportJpaRepository reportJpaRepository;

    @Override
    public boolean existByTargetIdAndReportIdAndReportType(Long targetId, Long reportId, ReportTargetType reportType) {
        return reportJpaRepository.existsByTargetIdAndReporterIdAndTargetType(targetId, reportId, reportType);
    }

    @Override
    public ReportEntity save(ReportEntity reportEntity) {
        return reportJpaRepository.save(reportEntity);
    }

    @Override
    public ReportEntity findById(Long reportId) {
        return reportJpaRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "신고를 찾을 수 없습니다"));
    }
    
    @Override
    public List<ReportEntity> findByTargetTypeAndStatus(ReportTargetType targetType, ReportStatus status) {
        return reportJpaRepository.findAll().stream()
                .filter(report -> report.getTargetType() == targetType && report.getStatus() == status)
                .collect(Collectors.toList());
    }
}
