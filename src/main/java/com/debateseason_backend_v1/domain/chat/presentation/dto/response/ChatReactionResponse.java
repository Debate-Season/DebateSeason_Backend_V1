package com.debateseason_backend_v1.domain.chat.model.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "이모티콘 반응 정보")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatReactionResponse {

    @Schema(description = "논리적 이모티콘 반응 수", example = "1")
    private int logicCount;

    @Schema(description = "태도 이모티콘 반응 수", example = "2")
    private int attitudeCount;

    @Schema(description = "현재 사용자의 논리적 이모티콘 반응 여부", example = "true")
    private boolean userReactedLogic;

    @Schema(description = "현재 사용자의 태도 이모티콘 반응 여부", example = "false")
    private boolean userReactedAttitude;


}
