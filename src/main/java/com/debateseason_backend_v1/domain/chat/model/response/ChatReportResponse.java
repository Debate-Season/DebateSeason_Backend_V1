package com.debateseason_backend_v1.domain.chat.model.response;


import com.debateseason_backend_v1.common.enums.ReportReasonType;
import com.debateseason_backend_v1.domain.repository.entity.ChatReport;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChatReportResponse {

    @Schema(description = "신고 ID", example = "1")
    private Long id;

    @Schema(description = "채팅 메시지 ID", example = "1045")
    private Long chatId;
    
    @Schema(description = "신고 유형 (여러 개 선택 가능)", example = "[\"ABUSE\", \"SPAM\"]")
    private List<ReportReasonType> reasonTypes;

    @Schema(description = "신고 상세 사유", example = "해당 사용자가 지속적으로 욕설을 사용하고 있습니다.")
    private String reasonDetail;

    @Schema(description = "신고 상태", example = "PENDING")
    private ChatReport.ReportStatus status;

    @JsonSerialize(using = LocalDateTimeSerializer.class) // 직렬화 시 필요
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // 역직렬화 시 필요
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    @Schema(description = "신고 일시", example = "2023-05-20T14:30:15")
    private LocalDateTime createdAt;

    public static ChatReportResponse from(ChatReport chatReport) {
        return ChatReportResponse.builder()
                .id(chatReport.getId())
                .chatId(chatReport.getChat().getId())
                .reasonTypes(chatReport.getReasonTypes())
                .reasonDetail(chatReport.getReasonDetail())
                .status(chatReport.getStatus())
                .createdAt(chatReport.getCreatedAt())
                .build();
    }
}
