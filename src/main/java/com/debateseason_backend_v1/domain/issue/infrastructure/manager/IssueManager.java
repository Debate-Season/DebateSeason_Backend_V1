package com.debateseason_backend_v1.domain.issue.infrastructure.manager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.issue.application.repository.IssueRepository;
import com.debateseason_backend_v1.domain.issue.mapper.IssueBriefResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IssueManager {

	private final IssueRepository issueRepository;

	// 1. 채팅방 수 내림차순 기준으로, 최상위 5개 선정 ( issue_Id만 추출 )
	private List<Long> findTop5ActiveIssuesByCountingChats(){

		// 최상위 5개의 이슈 id를 가져옴
		return issueRepository.findTop5ActiveIssuesByCountingChats().stream().map(
			e-> (Long)e[0]
		).toList();
	}

	// 2. 최상위 이슈방 5개 반환.
	public List<IssueBriefResponse> findTop5BestIssueRooms(){

		List<Long> issueIds = this.findTop5ActiveIssuesByCountingChats();

		// ui1.issue_id, ui1.title, ui1.created_at, ui1.chat_room_count, COUNT(ui2.issue_id) AS bookmarks

		return issueRepository.findIssuesWithBookmarksOrderByCreatedDate(issueIds).stream().map(
			e->{
				Long issueId = (Long)e[0];
				String title = (String)e[1];

				String time = e[2].toString();
				String result = time.split("\\.")[0];
				LocalDateTime createdAt = LocalDateTime.parse(result, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

				Long chatRoomCount = (Long)e[3];
				Long bookMarksCount = (Long)e[4];

				return IssueBriefResponse.builder()
					.issueId(issueId)
					.title(title)
					//.createdAt(createdAt)
					.countChatRoom(chatRoomCount)
					.bookMarks(bookMarksCount)
					.build()
					;

			}
		).toList();
	}



}
