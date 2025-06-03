package com.debateseason_backend_v1.domain.youtubeLive.model.response;

import java.util.List;

import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomMapper;
import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLiveDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// YoutubeLive 상세보기
@Getter
@Setter
@AllArgsConstructor
public class YoutubeLiveDetail {

	// 유튜브 라이브
	private YoutubeLiveDto youtubeLiveResponse;

	// 이슈 5건
	private List<ChatRoomMapper> top5BestChatRooms;

}
