package com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.dummy;

import java.util.List;

import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.messages.ChatForm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class HightlightResponse {
	private int totalLogic;
	private int totalAttitude;
	private List<ChatForm> highlightChats;
}
