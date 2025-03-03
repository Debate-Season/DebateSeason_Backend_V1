package com.debateseason_backend_v1.domain.chatroom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomDTO {

    @Schema(description = "제목",example = "DeepSeek 출시")
    private String title;

    @Schema(description = "내용",example = "#AI #IT")
    private String content;
}
