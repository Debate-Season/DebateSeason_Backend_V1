package com.debateseason_backend_v1.domain.chat.presentation.dto.report.request;

import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessReportRequest {
    @NotNull(message = "처리 상태는 필수입니다")
    private ReportStatus status;

    @Size(max = 500, message = "관리자 코멘트는 500자 이내로 작성해주세요")
    private String comment;
}
