package com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ChatForm {
	private String content;
	private int logic;
	private int attitude;
}
