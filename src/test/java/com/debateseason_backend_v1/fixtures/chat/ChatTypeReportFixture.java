package com.debateseason_backend_v1.fixtures.chat;

import com.debateseason_backend_v1.domain.chat.domain.model.report.Report;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportReasonType;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportStatus;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportTargetType;

import java.util.Set;

public class ChatTypeReportFixture {

    /**
     * @return NONE("신고 되지 않음") 상태의 Report 반환
     */
    public static Report createReport() {
        return Report.builder()
                .targetId(1L)
                .reporterId(2L)
                .targetType(ReportTargetType.CHAT)
                .reportReasonTypes(Set.of(ReportReasonType.ABUSE))
                .description("기본 신고")
                .build();
    }

    /**
     * @return PENDING("신고 접수됨") 상태의 Report 반환
     */
    public static Report createPendingReport(){
        return Report.builder()
                .targetId(1L)
                .reporterId(2L)
                .status(ReportStatus.PENDING)
                .targetType(ReportTargetType.CHAT)
                .reportReasonTypes(Set.of(ReportReasonType.ABUSE))
                .description("기본 신고")
                .build();
    }

    /**
     * @return ACCEPTED("신고 승인됨") 상태의 Report 반환
     */
    public static Report createAcceptedReport(){
        return Report.builder()
                .targetId(1L)
                .reporterId(2L)
                .targetType(ReportTargetType.CHAT)
                .reportReasonTypes(Set.of(ReportReasonType.ABUSE))
                .status(ReportStatus.ACCEPTED)
                .description("승인된 신고")
                .adminComment("승인 처리")
                .build();
    }

    /**
     * @return REJECTED("신고 거부됨") 상태의 Report 반환
     */
    public static Report createRejectedReport(){
        return Report.builder()
                .targetId(1L)
                .reporterId(2L)
                .targetType(ReportTargetType.CHAT)
                .reportReasonTypes(Set.of(ReportReasonType.ABUSE))
                .status(ReportStatus.REJECTED)
                .description("거보된 신고")
                .adminComment("거부 처리")
                .build();
    }
    /**
     * 커스텀 가능한 Report 빌더를 생성합니다.
     * 테스트에서 targetId, targetType, reportReasonType만 지정하고
     * 나머지는 필요에 따라 설정할 수 있습니다.
     *
     * @param targetId 신고 대상 ID
     * @param targetType 신고 대상 타입
     * @param reportReasonType 신고 이유
     * @return 커스터마이징 가능한 Report.ReportBuilder
     */
    public static Report.ReportBuilder createCustomReportBuilder(Long targetId,
                                                                       ReportTargetType targetType,
                                                                       ReportReasonType reportReasonType) {
        return Report.builder()
                .targetId(targetId)
                .reporterId(2L)
                .targetType(targetType)
                .reportReasonTypes(reportReasonType != null ? Set.of(reportReasonType) : null)
                .description("커스텀 가능한 테스트 신고");
    }
    /**
     * @return targetId 가 null 인 Report를 리턴합니다.
     */
    public static Report createTargetIdNullReport(){
        return createCustomReportBuilder(null,ReportTargetType.CHAT,ReportReasonType.ABUSE).build();
    }
    /**
     * @return targetId 가 null 인 Report를 리턴합니다.
     */
    public static Report createTargetTypeNullReport(){
        return createCustomReportBuilder(1L,null,ReportReasonType.ABUSE).build();
    }
    /**
     * @return targetId 가 null 인 Report를 리턴합니다.
     */
    public static Report createReasonTypeNullReport(){
        return createCustomReportBuilder(1L,ReportTargetType.CHAT,null).build();
    }

}
