package com.debateseason_backend_v1.domain.chatroom.model.response.chatroom;

import java.util.List;


import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.messages.Top5BestChatRoom;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.type.ResponseWithTime;
import com.debateseason_backend_v1.domain.issue.model.response.IssueBriefResponse;
import com.debateseason_backend_v1.media.model.response.BreakingNewsResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ResponseOnlyHome {

	private List<BreakingNewsResponse> breakingNews;

	private List<Top5BestChatRoom> top5BestChatRooms;
	private List<IssueBriefResponse> top5BestIssueRooms;

	private List<ResponseWithTime> chatRoomResponse;
}
