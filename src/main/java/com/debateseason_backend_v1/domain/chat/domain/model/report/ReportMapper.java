package com.debateseason_backend_v1.domain.chat.domain.model.report;

import com.debateseason_backend_v1.domain.chat.infrastructure.report.ReportEntity;

public class ReportMapper {

    //TODO: create() 가아니고 bilder() 를 사용해얃...751212

    public static Report toDomain(ReportEntity entity) {
        return Report.create(
                entity.getTargetId(),
                entity.getReporterId(),
                entity.getTargetType(),
                entity.getReportReasonTypes(),
                entity.getDescription()
        );
    }

    public static ReportEntity toEntity(Report report) {
        return ReportEntity.builder()
                .id(report.getId())
                .targetId(report.getTargetId())
                .reporterId(report.getReporterId())
                .targetType(report.getTargetType())
                .reportReasonTypes(report.getReportReasonTypes())
                .status(report.getStatus())
                .description(report.getDescription())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
