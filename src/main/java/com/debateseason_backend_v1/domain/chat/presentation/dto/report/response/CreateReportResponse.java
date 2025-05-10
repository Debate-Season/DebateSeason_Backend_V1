package com.debateseason_backend_v1.domain.chat.presentation.dto.report.response;

import com.debateseason_backend_v1.domain.chat.domain.model.report.Report;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateReportResponse {

    private String message;

    public static CreateReportResponse from() {
        return CreateReportResponse.builder()
                .message("")
                .build();
    }
}
