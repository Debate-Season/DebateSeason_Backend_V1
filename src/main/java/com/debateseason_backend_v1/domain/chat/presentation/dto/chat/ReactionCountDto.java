package com.debateseason_backend_v1.domain.chat.presentation.dto.chat;

import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatReactionRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReactionCountDto {
    private final Long chatId;
    private final ChatReactionRequest.ReactionType reactionType;
    private final Long count;
}
