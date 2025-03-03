package com.debateseason_backend_v1.domain.repository;

import java.util.List;

import com.debateseason_backend_v1.domain.repository.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends JpaRepository<Issue,Long> {
	


	
	// issue방 페이지네이션
	@Query(value = "SELECT issue_id FROM issue " +
		"WHERE issue_id <= (SELECT COUNT(issue_id) - :page FROM issue) " +
		"ORDER BY issue_id DESC " +
		"LIMIT 2",
		nativeQuery = true)
	List<Long> findIssuesByPage(@Param("page") Long page);



	// issue-map에서 이슈 id 가져오기
	// "전체 선택"을 한 경우 또는 또는 "최초요청"
	@Query(value = "SELECT issue_id FROM issue  "
		+ " WHERE issue_id < :page "
		+ " ORDER BY issue_id DESC "
		+ " LIMIT 2", nativeQuery = true)
	List<Long> findTop2IssuesByPage(@Param("page") Long page);

	@Query(value = "SELECT issue_id FROM issue "
		+ "ORDER BY issue_id DESC "
		+ "LIMIT 2", nativeQuery = true)
	List<Long> findTop2Issues();

	// majorCategory만 설정을 한 경우
	@Query(value = "SELECT issue_id FROM issue "
		+ " WHERE major_category = :majorCategory "
		+ "ORDER BY issue_id DESC "
		+ "LIMIT 2", nativeQuery = true)
	List<Long> findTop2IssuesByCategory(@Param("majorCategory")String majorCategory);

	// majorCategory + page 설정을 한 경우
	@Query(value = "SELECT issue_id FROM issue"
		+ " WHERE major_category = :majorCategory AND issue_id < :page "
		+ "ORDER BY issue_id DESC "
		+ "LIMIT 2", nativeQuery = true)
	List<Long> findTop2IssuesByPageAndCategory(
		@Param("majorCategory")String majorCategory,
		@Param("page") Long page
		);

	// 이슈방 불러오기( 채팅방 카운트 + 즐겨찾기 수 카운트)
	@Query(value = """
        SELECT ui1.issue_id, ui1.title, ui1.created_at, ui1.chat_room_count, COUNT(ui2.issue_id) AS bookmarks
        FROM (
            SELECT i.issue_id, i.title, i.created_at, COUNT(ch.issue_id) AS chat_room_count
            FROM issue i
            LEFT JOIN chat_room ch ON i.issue_id = ch.issue_id
            WHERE i.issue_id IN (:issueIds)
            GROUP BY i.issue_id
        ) ui1
        LEFT JOIN user_issue ui2 ON ui1.issue_id = ui2.issue_id
        GROUP BY ui1.issue_id
        ORDER BY ui1.issue_id DESC
        """, nativeQuery = true)
	List<Object[]> findIssuesWithBookmarks(@Param("issueIds") List<Long> issueIds);
}



