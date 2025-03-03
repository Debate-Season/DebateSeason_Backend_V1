package com.debateseason_backend_v1.domain.issue.dto;

import java.util.List;
import java.util.Map;

import com.debateseason_backend_v1.domain.chatroom.dto.ChatRoomDAO;
import com.debateseason_backend_v1.domain.chatroom.model.response.ChatRoomResponse;

import io.swagger.v3.oas.annotations.media.Schema;
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
	@Schema(description = "이슈명", example = "윤석열 계엄")
	private String title;

	@Schema(description = "커뮤니티 목록", example = "{ community/icons/dcinside.png: 1 }")
	private Map<String, Integer> map;

	@Schema(description = "채팅방 목록", example = "")
	private List<ChatRoomResponse> chatRoomMap;
}
