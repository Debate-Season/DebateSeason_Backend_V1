package com.debateseason_backend_v1.domain.chatroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ResponseDTO {
	private ChatRoomDAO chatRoomDAO;
	//private List<ChatDAO> chatList;
}
