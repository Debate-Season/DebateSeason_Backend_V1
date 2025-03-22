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

	private Long issueId;
	private String issueTitle;
	private Long debateId;
	private String debateTitle;
	private String time;
}
