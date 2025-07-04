package com.debateseason_backend_v1.domain.chat.domain.model.report;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
public class Report {

    private Long id;
    private final Long targetId;
    private final Long reporterId;
    private final ReportTargetType targetType;
    private ReportStatus status;
    private final Set<ReportReasonType> reportReasonTypes;
    private final String description;
    private final LocalDateTime createdAt;
    private LocalDateTime processAt;
    private String adminComment;
    private Long processedById;

    @Builder
    private Report(Long targetId, Long reporterId, ReportTargetType targetType,
                   Set<ReportReasonType> reportReasonTypes, String description,
                   ReportStatus status, LocalDateTime processAt,
                   String adminComment, Long processedById) {

        validateCreationArguments(targetId, targetType, reportReasonTypes);

        this.targetId = targetId;
        this.reporterId = reporterId;
        this.targetType = targetType;
        this.reportReasonTypes = reportReasonTypes;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        //테스트용 필드
        this.status = status != null ? status : ReportStatus.NONE;
        this.processAt = processAt;
        this.adminComment = adminComment;
        this.processedById = processedById;
    }

    public static Report create (Long targetId, Long reporterId,ReportTargetType targetType, Set<ReportReasonType> reportReasonTypes, String description) {
        validateCreationArguments(targetId, targetType, reportReasonTypes);
        return  Report.builder()
                .targetId(targetId)
                .reporterId(reporterId)
                .targetType(targetType)
                .reportReasonTypes(reportReasonTypes)
                .description(description)
                .build();
    };

    //신고를 접수 한다.
    public void markAsProcessing() {
        guardStatusChange();
        this.status = ReportStatus.PENDING;
    }

    //신고를 거부 한다
    public void reject(Long adminId, String adminComment){
        guardStatusChange();
        this.status = ReportStatus.REJECTED;
        this.processAt = LocalDateTime.now();
        this.adminComment = adminComment;
        this.processedById = adminId;
    }

    //신고를 승인 한다.
    public void accept(Long adminId, String adminComment){
        guardStatusChange();
        this.status = ReportStatus.ACCEPTED;
        this.processAt = LocalDateTime.now();
        this.adminComment = adminComment;
        this.processedById = adminId;
    }

    //이미 처리된 신고인 경우 상태 변경을 방어 한다.
    public void guardStatusChange(){
        if(status == ReportStatus.ACCEPTED || status == ReportStatus.REJECTED){
            throw new CustomException(ErrorCode.REPORT_ALREADY_PROCESSED);
        }
    }

    private static void validateCreationArguments(Long targetId, ReportTargetType targetType, Set<ReportReasonType> reportReasonTypes){
        if (targetId == null) throw new IllegalArgumentException("신고 대상 ID(targetId)는 필수 입니다");
        if (targetType == null) throw new IllegalArgumentException("신고 대상 타입(targetType)은 필수 입니다");
        if (reportReasonTypes == null) throw new IllegalArgumentException("신고 이유(reasonType)는 필수 입니다.");
    }

}
