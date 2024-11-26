package com.debateseason_backend_v1.domain.chat.model.response;

import com.debateseason_backend_v1.domain.chat.model.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "채팅 리스트 Response")
public record ChatListResponse(

        @Schema(description = "채팅 메시지 목록")
        List<ChatMessage> result,

        @Schema(description = "총 메시지 수", example = "10")
        int totalNumberOfMessages
) {}
