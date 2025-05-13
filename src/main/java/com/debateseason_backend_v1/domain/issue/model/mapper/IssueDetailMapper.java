package com.debateseason_backend_v1.domain.issue.model.mapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.type.ResponseWithTimeAndOpinion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueDetailMapper {
	private String title;
	private String bookMarkState;
	private Long bookMarks;
	private Long chats;
	private LinkedHashMap<String, Integer> map;
	private List<ResponseWithTimeAndOpinion> chatRoomMap;
}
