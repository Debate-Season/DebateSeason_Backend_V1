package com.debateseason_backend_v1.domain.youtubeLive.model.response;

import java.util.List;

import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;

// YoutubeLive 상세보기
@Getter
@AllArgsConstructor
public class YoutubeLiveDetail {

	// 유튜브 라이브
	private final YoutubeLiveResponse youtubeLiveResponse;

	// 이슈 5건
	private final List<ChatRoomMapper> top5BestChatRooms;

}
