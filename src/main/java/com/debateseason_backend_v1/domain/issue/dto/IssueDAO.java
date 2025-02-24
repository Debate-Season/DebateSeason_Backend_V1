package com.debateseason_backend_v1.domain.issue.dto;

import java.util.List;
import java.util.Map;

import com.debateseason_backend_v1.domain.chatroom.dto.ChatRoomDAO;
import com.debateseason_backend_v1.domain.chatroom.model.response.ChatRoomResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class IssueDAO {

	public IssueDAO() {

	}

	private String title;
	private Map<String, Integer> map;
	private List<ChatRoomResponse> chatRoomMap;
}
