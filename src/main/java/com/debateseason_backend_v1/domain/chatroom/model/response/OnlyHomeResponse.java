package com.debateseason_backend_v1.domain.chatroom.model.response;

import java.util.List;

import com.debateseason_backend_v1.domain.issue.model.response.IssueBriefResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class OnlyHomeResponse {
	private List<Top5BestChatRoom> top5BestChatRooms;
	private List<IssueBriefResponse> top5BestIssueRooms;

	private List<IssueBriefResponse> chatRoomResponse;
}
