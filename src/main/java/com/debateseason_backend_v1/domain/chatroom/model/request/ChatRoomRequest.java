package com.debateseason_backend_v1.domain.chatroom.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomRequest {

    @Schema(description = "제목",example = "DeepSeek 출시")
    private String title;

    @Schema(description = "내용",example = "#AI #IT")
    private String content;
}
