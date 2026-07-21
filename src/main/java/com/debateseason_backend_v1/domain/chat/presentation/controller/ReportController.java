package com.debateseason_backend_v1.domain.chat.presentation.controller;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chat.application.service.ReportService;
import com.debateseason_backend_v1.domain.chat.presentation.dto.report.request.ReportRequest;
import com.debateseason_backend_v1.domain.chat.presentation.dto.report.response.CreateReportResponse;
import com.debateseason_backend_v1.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@RestController
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/messages/{messageId}/report")
    public ApiResult<Object> reportChatMessage(
            @RequestBody ReportRequest reportRequest,
            @PathVariable Long messageId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        // 이전에는 신고자 userId 가 어노테이션 없는 파라미터라 요청 파라미터로 바인딩됐다.
        // 즉 클라이언트가 임의의 userId 로 남을 신고할 수 있었다.
        // (호출하던 authService.getCurrentUserId() 의 반환값은 버려지고 있었다)
        if (principal == null) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN, "인증 정보를 확인할 수 없습니다");
        }

        reportService.createChatReport(
                messageId,
                principal.getUserId(),
                reportRequest.getReasonType(),
                reportRequest.getReasonDetail()
        );

        return ApiResult.success("메시지가 신고 되었습니다.", CreateReportResponse.from());
    }

}
