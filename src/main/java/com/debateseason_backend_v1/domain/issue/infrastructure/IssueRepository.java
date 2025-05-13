package com.debateseason_backend_v1.domain.issue.infrastructure;

import java.util.List;

import com.debateseason_backend_v1.domain.repository.entity.Issue;

public interface IssueRepository {

	// 기본적인 CRUD
	void save(Issue issue);

	Issue findById(Long issueId);

	// 1. 커스텀 기능

	// 1. 각 이슈와 그에 해당하는 북마크 가져오기
	List<Object[]> findIssueWithBookmarks(Long issueId);
	
	// 2. issue id 기반으로 오늘 채팅수 가져오기
	Long countChatsTodayByIssueId(Long issueId);

	// 3 모든 Issue 가져오기
	List<Issue> findAll();


	// 2.페이지네이션 전용 기능

	// 2.1 최초 api 호출시 최신 이슈 6건을 불러온다.
	List<Long> findTop6Issues(int cursorSize);

	// 2.1을 위한 커서 기반 페이지네이션
	List<Long> findTop6IssuesByPage(Long page,int cursorSize);

	// 2.2 최초 api 호출시 카테고리 기반으로 이슈 가져오기
	List<Long> findTop6IssuesByCategory(String majorCategory,int cursorSize);

	// 2.2을 위한 커서 기반 페이지네이션
	List<Long> findTop6IssuesByPageAndCategory(String majorCategory,Long page,int cursorSize);

	List<Object[]> findIssueCountingBookmarkAndChatRoom(List<Long> issueIds);

	// 3. 채팅방에 사용되는 이슈 기능

	// 3-1.
	// 5개의 이슈방을 가져오는데 오늘 기준 가장 많은 채팅이 있는 것들을 가져온다.(이슈방 id)
	List<Object[]> findTop5ActiveIssuesByCountingChats();

	// 바로 이어 그 위에서 가져온 이슈방 id를 이용해서 실제 레코드들을 불러온다.
	List<Object[]>findIssuesWithBookmarksOrderByCreatedDate(List<Long> issueIds);
}
