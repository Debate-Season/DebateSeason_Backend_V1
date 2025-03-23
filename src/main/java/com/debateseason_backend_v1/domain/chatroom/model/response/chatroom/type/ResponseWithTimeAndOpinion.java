package com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.type;

import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.ChatRoomTemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ResponseWithTimeAndOpinion extends ChatRoomTemplate {
	@Schema(description = "최초 대화 생성후 몇 분 초과",example = "29분 전 대화")
	private String time;

	@Setter
	@Schema(description = "의견",example = "반대")
	private String opinion;
}
