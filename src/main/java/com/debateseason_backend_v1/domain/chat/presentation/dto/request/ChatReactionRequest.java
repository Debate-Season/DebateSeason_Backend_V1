package com.debateseason_backend_v1.domain.chat.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "이모티콘 반응 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatReactionRequest {
    
    @Schema(description = "반응 타입 (LOGIC 또는 ATTITUDE)", example = "LOGIC")
    @NotNull(message = "반응 타입은 필수입니다")
    private ReactionType reactionType;
    
    @Schema(description = "반응 동작 (ADD 또는 REMOVE)", example = "ADD")
    @NotNull(message = "반응 동작은 필수입니다")
    private ReactionAction action;
    
    public enum ReactionType {
        LOGIC, ATTITUDE
    }
    
    public enum ReactionAction {
        ADD, REMOVE
    }
}