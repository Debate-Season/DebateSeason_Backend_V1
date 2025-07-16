package com.debateseason_backend_v1.domain.issue.mapper;

import java.util.List;
import java.util.Map;

import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.type.ResponseWithTimeAndOpinion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Builder
public class IssueRoomDetailMapper { // 일관성, GC 범위 감소

	@Schema(description = "이슈명", example = "윤석열 계엄")
	private final String title;

	@Schema(description = "즐겨찾기", example = "yes")
	private final String bookMarkState;

	@Schema(description = "관심등록", example = "1005")
	private final Long bookMarks;

	@Schema(description = "오늘 신규 대화", example = "300")
	private final Long chats;

	@Schema(description = "커뮤니티 목록", example = "{ community/icons/dcinside.png: 1 }")
	private final Map<String, Integer> map;

	@Schema(description = "채팅방 목록", example = "")
	private final List<ResponseWithTimeAndOpinion> chatRoomMap;

}
