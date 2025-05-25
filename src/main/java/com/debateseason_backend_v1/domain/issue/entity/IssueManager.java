package com.debateseason_backend_v1.domain.issue.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.debateseason_backend_v1.domain.issue.PaginationDTO;
import com.debateseason_backend_v1.domain.issue.model.mapper.IssueDetailMapper;
import com.debateseason_backend_v1.domain.issue.model.mapper.IssueMapper;
import com.debateseason_backend_v1.domain.issue.model.response.IssueBriefResponse;
import com.debateseason_backend_v1.domain.issue.model.response.IssueDetailResponse;

public class IssueManager {

	// 1. 이슈맵 전용 쿼리
	public PaginationDTO createIssueForIssueMapOnly(List<Object[]> issues){

		List<IssueBriefResponse> issueBriefResponse =  issues.stream().map(
			e->{
				Long issueId = (Long)e[0];
				String title = (String)e[1];

				String time = e[2].toString();
				String result = time.split("\\.")[0];
				LocalDateTime createdAt = LocalDateTime.parse(result, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

				Long chatRoomCount = e[4] == null ? 0 : ((Number)e[4]).longValue();
				Long bookMarkCount = e[3] == null ? 0 : ((Number)e[3]).longValue();

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

		// 오직 데이터 가공용도
		PaginationDTO paginationDTO = new PaginationDTO();
		paginationDTO.setItems(issueBriefResponse);

		return paginationDTO;
	}

	// 2. issue_id, title, COUNT(ui.issue_id) AS bookmarks 가공해주는 메서드
	public static IssueMapper findIssueWithIdAndTitleAndBookMarks(List<Object[]> issue){

		IssueMapper issueMapper = new IssueMapper();

		for(Object[] obj : issue){
			issueMapper.setTitle((String)obj[1]);
			issueMapper.setBookMarks((Long)obj[2]);
		}

		return issueMapper;
	}

	// 1.chatRoom이 없는 경우
	public static IssueDetailResponse createIssueDetailResponseWithNoChatRooms(IssueDetailMapper e){

		return IssueDetailResponse.builder()
			.title(e.getTitle())
			.bookMarkState(e.getBookMarkState())
			.bookMarks(e.getBookMarks())
			.map(e.getMap())
			.build();

	}

	// 1.chatRoom이 있는경우
	public static IssueDetailResponse createIssueDetailResponseWithChatRooms(IssueDetailMapper e){

		return IssueDetailResponse.builder()
			.title(e.getTitle())
			.bookMarkState(e.getBookMarkState())
			.bookMarks(e.getBookMarks())
			.chats(e.getChats())
			.map(e.getMap())
			.chatRoomMap(e.getChatRoomMap())
			.build();

	}

	// 최상위 5건 이슈방 가져오기
	public List<IssueBriefResponse> getTop5Issues(List<Object[]> issues){
		return issues.stream().map(
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
