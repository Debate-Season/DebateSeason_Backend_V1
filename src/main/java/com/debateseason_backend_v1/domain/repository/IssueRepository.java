package com.debateseason_backend_v1.domain.repository;

import java.util.List;

import com.debateseason_backend_v1.domain.repository.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends JpaRepository<Issue,Long> {
	
	// issueMap을 위한 쿼리인데 나중에 middle_category도 추가할 듯.
	@Query(value = """
    SELECT i1.issue_id, i1.title, i1.major_category, i1.created_at
    FROM issue i1,
         (
            SELECT * FROM issue
            WHERE major_category = :majorCategory
            ORDER BY issue_id DESC
            LIMIT 2
            OFFSET :page
         ) i2
    WHERE i1.issue_id = i2.issue_id 
    ORDER BY i1.issue_id DESC
    """, nativeQuery = true)
	List<Object[]> findIssuesByMajorCategoryAndPage(
		@Param("majorCategory") String majorCategory,
		@Param("page") Long page);


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
	
	// issue방 페이지네이션
	@Query(value = "SELECT issue_id FROM issue " +
		"WHERE issue_id <= (SELECT COUNT(issue_id) - :page FROM issue) " +
		"ORDER BY issue_id DESC " +
		"LIMIT 2",
		nativeQuery = true)
	List<Long> findIssuesByPage(@Param("page") Long page);

	// issuMap 카테고리 + 페이지네이션
	@Query(value = "SELECT issue_id "
		+ "FROM issue "
		+ "WHERE major_category = (:majorCategory) "
		+ "ORDER BY issue_id DESC "
		+ "LIMIT 2 "
		+ "OFFSET :page ", nativeQuery = true)
	List<Long> findIssuesByMajorCategoryWithPage(
		@Param("majorCategory") String majorCategory,
		@Param("page") Long page);



}



