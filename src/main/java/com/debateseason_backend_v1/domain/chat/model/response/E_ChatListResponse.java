package com.debateseason_backend_v1.domain.chat.model.response;

import com.debateseason_backend_v1.domain.chat.model.Message;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "채팅 리스트 Response")
public record E_ChatListResponse(

        @Schema(description = "return Message : []")
        List<Message> result
) {}
