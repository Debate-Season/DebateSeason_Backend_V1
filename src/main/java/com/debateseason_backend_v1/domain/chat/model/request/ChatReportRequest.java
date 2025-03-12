package com.debateseason_backend_v1.domain.chat.model.request;

import com.debateseason_backend_v1.common.enums.ReportReasonType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatReportRequest {

    @NotEmpty(message = "신고 유형은 최소 하나 이상 선택해야 합니다.")
    @Schema(description = "신고 유형 (여러 개 선택 가능)", example = "[\"ABUSE\", \"SPAM\"]")
    private List<ReportReasonType> reasonTypes;

    @Size(min = 0, max = 100, message = "상세 사유는 100자 이하로 작성해주세요.")
    @Schema(description = "신고 상세 사유 (100자 이하)", example = "해당 사용자가 지속적으로 욕설을 사용하고 있습니다.")
    private String reasonDetail;
    
    public ReportReasonType getReasonType() {
        return reasonTypes != null && !reasonTypes.isEmpty() ? reasonTypes.get(0) : null;
    }
    
    public void setReasonType(ReportReasonType reasonType) {
        if (reasonType != null) {
            if (this.reasonTypes == null) {
                this.reasonTypes = new ArrayList<>();
            }
            if (!this.reasonTypes.contains(reasonType)) {
                this.reasonTypes.add(reasonType);
            }
        }
    }
    
    public void setReasonType(String reasonTypeStr) {
        if (reasonTypeStr != null && !reasonTypeStr.isEmpty()) {
            if (this.reasonTypes == null) {
                this.reasonTypes = new ArrayList<>();
            }
            
            String[] types = reasonTypeStr.split(",");
            for (String type : types) {
                try {
                    ReportReasonType reasonType = ReportReasonType.valueOf(type.trim());
                    if (!this.reasonTypes.contains(reasonType)) {
                        this.reasonTypes.add(reasonType);
                    }
                } catch (IllegalArgumentException e) {
                    // 유효하지 않은 열거형 값은 무시
                }
            }
        }
    }
}
