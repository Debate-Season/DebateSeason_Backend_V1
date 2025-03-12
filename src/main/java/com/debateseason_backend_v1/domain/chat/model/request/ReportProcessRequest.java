package com.debateseason_backend_v1.domain.chat.model.request;


import com.debateseason_backend_v1.domain.repository.entity.ChatReport;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportProcessRequest {

    @NotNull(message = "처리 상태는 필수입니다.")
    @Schema(description = "신고 처리 상태", example = "ACCEPTED")
    private ChatReport.ReportStatus status;

    @Schema(description = "처리 사유", example = "커뮤니티 가이드라인 위반으로 확인되어 신고를 승인합니다.")
    private String comment;
}
