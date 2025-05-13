package com.debateseason_backend_v1.domain.chat.presentation.dto.chat.response;

import com.debateseason_backend_v1.common.enums.MessageType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageErrorResponse {
    private MessageType messageType;
    private String message;
}
