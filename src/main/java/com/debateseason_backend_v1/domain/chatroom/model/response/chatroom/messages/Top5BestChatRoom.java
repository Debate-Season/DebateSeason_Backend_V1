package com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Top5BestChatRoom {
	private Long chatRoomId;
	private String title;
	private String content;
}
