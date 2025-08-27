package com.debateseason_backend_v1.domain.issue.infrastructure.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.issue.mapper.IssueBriefResponse;

// DB -> DTO로 처리
@Component
public class IssueProcessorManager {

	/*  IssueBriefResponse 필드
		Long issueId;
		String title;
		String time;
		LocalDateTime createdAt = LocalDateTime.parse(result,
		Long chatRoomCount;
		Long bookMarkCount;
	 */
	public List<IssueBriefResponse> createIssueBriefResponse(List<Object[]> objects){

		return objects.stream().map( // 순서에 유의하세요!
			e -> {
				Long issueId = (Long)e[0];
				String title = (String)e[1];

				String time = e[2].toString();
				String result = time.split("\\.")[0];
				LocalDateTime createdAt = LocalDateTime.parse(result,
					DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

				Long chatRoomCount = (Long)e[3];
				Long bookMarkCount = (Long)e[4];

				return IssueBriefResponse.builder()
					.issueId(issueId)
					.title(title)
					.createdAt(createdAt)
					.countChatRoom(chatRoomCount)
					.bookMarks(bookMarkCount)
					.build()
					;
			}
		).toList();

	}
}
