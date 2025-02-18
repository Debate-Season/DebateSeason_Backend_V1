package com.debateseason_backend_v1.domain.chat.model.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatMessagesResponse {
    private List<ChatMessagesByDate> messagesByDates;
    private String nextCursor;
}
