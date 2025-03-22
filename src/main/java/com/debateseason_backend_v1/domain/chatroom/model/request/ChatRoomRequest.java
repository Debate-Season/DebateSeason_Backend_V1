package com.debateseason_backend_v1.domain.chatroom.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomRequest {

    @NotBlank(message = "제목을 입력하세요")
    @Schema(description = "제목",example = "DeepSeek 출시")
    private String title;

    @NotBlank(message = "내용을 입력하세요")
    @Schema(description = "내용",example = "#AI #IT")
    private String content;
}
