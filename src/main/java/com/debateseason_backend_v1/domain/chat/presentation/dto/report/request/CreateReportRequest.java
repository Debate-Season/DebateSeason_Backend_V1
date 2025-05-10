package com.debateseason_backend_v1.domain.chat.presentation.dto.report.request;


import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportReasonType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class CreateReportRequest {
    @NotEmpty(message = "신고 사유는 최소 하나 이상 선택해야 합니다")
    @Size(max = 3, message = "신고 사유는 최대 3개까지 선택 가능합니다")
    private Set<ReportReasonType> reasons;

    @Size(max = 500, message = "상세 설명은 500자 이내로 작성해주세요")
    private String description;
}
