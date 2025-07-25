package com.debateseason_backend_v1.domain.issue.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;

public interface IssueRepository {

	// 기본적인 CRUD
	void save(IssueEntity issueEntity);

	IssueEntity findById(Long issueId);



	// 1. 이슈방 페이지 네이션
	List<Long> findIssuesByPage(@Param("page") Long page);

	// 2. 이슈맵에서 이슈 id 가져오기
	// "전체 선택"을 한 경우 또는 또는 "최초요청"
	List<Long> findTop6IssuesByPage(@Param("page") Long page);

	// 3. 이슈 중에서 최상위 6개 가져오기
	List<Long> findTop6Issues();

	// 4. 이슈 + 카테고리 필터링해서 최상위 6건 이슈 가져오기
	// majorCategory만 설정을 한 경우
	List<Long> findTop6IssuesByCategory(@Param("majorCategory")String majorCategory);

	// majorCategory + page 설정을 한 경우
	List<Long> findTop6IssuesByPageAndCategory(
		@Param("majorCategory")String majorCategory,
		@Param("page") Long page
	);

	// 이슈방 불러오기( 채팅방 카운트 + 즐겨찾기 수 카운트)
	List<Object[]> findIssuesWithBookmarks(@Param("issueIds") List<Long> issueIds);


	List<Object[]> findIssuesWithBookmarksOrderByCreatedDate(@Param("issueIds") List<Long> issueIds);


	List<Object[]> findSingleIssueWithBookmarks(@Param("issueId") Long issueId);


	List<Object[]> findTop5ActiveIssuesByCountingChats();

	// 즐겨찾기 이미 했나 안했나?
	//@Query(value = "SELECT id, user_id, issue_id, bookmark FROM user_issue WHERE issue_id = :issueId AND user_id = :userId", nativeQuery = true)
	//List<Object[]> findByIssueIdAndUserId(@Param("issueId") Long issueId, @Param("userId") Long userId);

	// 오늘 신규 채팅방 수
	Long countChatsTodayByIssueId(@Param("issueId") Long issueId);
}
