package com.debateseason_backend_v1.domain.chat.presentation.controller;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.auth.service.AuthServiceV1;
import com.debateseason_backend_v1.domain.chat.application.service.ReportService;
import com.debateseason_backend_v1.domain.chat.domain.model.report.Report;
import com.debateseason_backend_v1.domain.chat.presentation.dto.report.request.ReportRequest;
import com.debateseason_backend_v1.domain.chat.presentation.dto.report.response.CreateReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@RestController
public class ReportController {

    private final ReportService reportService;
    private final AuthServiceV1 authService;

    @PostMapping("/messages/{messageId}/report")
    public ApiResult<Object> reportChatMessage(@RequestBody ReportRequest reportRequest, @PathVariable Long messageId, Long userId) {
        authService.getCurrentUserId();
        Report chatReport = reportService.createChatReport(
                messageId,
                userId,
                reportRequest.getReasonType(),
                reportRequest.getReasonDetail()
        );

        return ApiResult.success("메시지가 신고 되었습니다.", CreateReportResponse.from());

    }

}
