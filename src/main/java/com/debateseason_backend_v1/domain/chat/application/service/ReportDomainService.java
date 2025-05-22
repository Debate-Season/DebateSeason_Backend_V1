package com.debateseason_backend_v1.domain.chat.application.service;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.chat.application.repository.ReportRepository;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ReportDomainService {

    private final ReportRepository reportRepository;

    /**
     * 동일한 대상이 중복 신고 하는지 검증 한다.
     * @throws CustomException ErrorCode.ALREADY_REPORTED;
    */
    public void validateNotAlreadyReport(Long targetId, Long reportId, ReportTargetType reportTargetType) {
        if (reportRepository.existByTargetIdAndReportIdAndReportType(
                targetId,
                reportId,
                reportTargetType)
            ){
            throw new CustomException(ErrorCode.ALREADY_REPORTED);
        }
    }
}
