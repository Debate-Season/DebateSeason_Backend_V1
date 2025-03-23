package com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.type;

import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.ChatRoomTemplate;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ResponseWithTime extends ChatRoomTemplate {
	private String time;
}
