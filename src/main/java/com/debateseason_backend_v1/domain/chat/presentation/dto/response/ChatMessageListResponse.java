package com.debateseason_backend_v1.domain.chat.model.response;

import com.debateseason_backend_v1.domain.chat.model.request.ChatMessageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "채팅 리스트 Response")
public record ChatMessageListResponse(

        @Schema(description = "채팅 메시지 목록")
        List<ChatMessageRequest> result,

        @Schema(description = "총 메시지 수", example = "10")
        int totalNumberOfMessages
) {}
