package com.debateseason_backend_v1.domain.issue.infrastructure;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.repository.entity.Issue;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class IssueRepositoryImpl implements IssueRepository{

	private final IssueJpaRepository issueJpaRepository;

	// 기본적인 CRUD

	@Override
	public void save(Issue issue) {
		issueJpaRepository.save(issue);
	}

	@Override
	public Issue findById(Long issueId) {
		return issueJpaRepository.findById(issueId)
			.orElseThrow(
				()-> new CustomException(ErrorCode.NOT_FOUND_ISSUE)
			);
	}

	@Override
	public List<Issue> findAll() {
		return issueJpaRepository.findAll();
	}


	// 커스텀 기능

	// 1. 여러 건의 이슈와 각각의 북마크 가져오기
	@Override
	public List<Object[]> findIssueWithBookmarks(Long issueId) {
		return issueJpaRepository.findIssueWithBookmarks(issueId);
	}

	// 2. issue_Id 기반으로 오늘 신규 채팅 건수 가져오기
	@Override
	public Long countChatsTodayByIssueId(Long issueId) {
		return issueJpaRepository.countChatsTodayByIssueId(issueId);
	}



	// 2.1 최초 api 요청시 최신 Issue를 가져오는 것과 페이지네이션을 위한 메소드
	@Override
	public List<Long> findTop6Issues(int cursorSize) {
		return issueJpaRepository.findTop6Issues(cursorSize);
	}

	@Override
	public List<Long> findTop6IssuesByPage(
		Long page,
		int cursorSize) {
		return issueJpaRepository.findTop6IssuesByPage(page,cursorSize);
	}

	// 2.2 최초 api 요청시 category 기반으로 최신 Issue를 가져오는 것과 페이지네이션을 위한 메소드
	@Override
	public List<Long> findTop6IssuesByCategory(
		String majorCategory,
		int cursorSize) {
		return issueJpaRepository.findTop6IssuesByCategory(majorCategory,cursorSize);
	}

	@Override
	public List<Long> findTop6IssuesByPageAndCategory(
		String majorCategory,
		Long page,
		int cursorSize) {
		return issueJpaRepository.findTop6IssuesByPageAndCategory(majorCategory,page,cursorSize);
	}

	// 3. Issue를 북마크수와 채팅방 함께 가져오는 메소드
	@Override
	public List<Object[]> findIssueCountingBookmarkAndChatRoom(List<Long> issueIds) {
		return issueJpaRepository.findIssueCountingBookmarkAndChatRoom(issueIds);
	}

	// 4. 채팅방에 사용되는 이슈방 메소드

	// 4-1. 채팅 수 기준으로 가장 많이 활성화 된 채팅 상위 5건 이슈방 id 가져오기
	@Override
	public List<Object[]> findTop5ActiveIssuesByCountingChats() {
		return issueJpaRepository.findTop5ActiveIssuesByCountingChats();
	}

	// 위에 이어서 가져온 이슈방 id로 실제 레코드 가져오기
	@Override
	public List<Object[]> findIssuesWithBookmarksOrderByCreatedDate(List<Long> issueIds) {
		return issueJpaRepository.findIssuesWithBookmarksOrderByCreatedDate(issueIds);
	}



}
