package com.debateseason_backend_v1.domain.chat.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatMessagesByDate {
    private String date;
    private List<ChatMessageResponse> chatMessageResponses;
    private boolean hasMore;
    private long totalCount;
}
