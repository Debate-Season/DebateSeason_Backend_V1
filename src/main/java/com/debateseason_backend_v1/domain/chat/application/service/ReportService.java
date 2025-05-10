package com.debateseason_backend_v1.domain.chat.application.service;

import com.debateseason_backend_v1.domain.chat.domain.model.report.Report;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportReasonType;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportStatus;

import java.util.List;
import java.util.Set;

public interface ReportService {

    /**
     * 신고를 생성 합니다.
    */
    Report createChatReport(Long messageId, Long reporterId, Set<ReportReasonType> reportReasonType, String description);

    /**
     * 특정 상태의 신고를 조회 합니다.
     */
    List<Report> getReportsByStatus(ReportStatus status, int page , int size);

    /**
     * 신고를 접수 합니다.
     */
    void processReportStatusAsMarkPending(Report report);


    /**
     * 신고를 처리 합니다.
     */
    public void processReport(Long reportId, Long adminId, ReportStatus newStatus, String comment);

}
