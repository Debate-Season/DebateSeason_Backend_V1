package com.debateseason_backend_v1.domain.chat.infrastructure.report;

import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportJpaRepository extends JpaRepository<ReportEntity, Long> {

    boolean existsByTargetIdAndReporterIdAndTargetType(Long targetId, Long reportId, ReportTargetType targetType);



}
