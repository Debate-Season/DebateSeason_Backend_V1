package com.debateseason_backend_v1.domain.repository;

import java.util.List;

import com.debateseason_backend_v1.domain.repository.entity.Issue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueJpaRepository extends JpaRepository<Issue,Long> {
	


	
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
		+ " LIMIT :cursorSize", nativeQuery = true)
	List<Long> findTop6IssuesByPage(
		@Param("page") Long page,
		@Param("cursorSize") int cursorSize
		);

	@Query(value = "SELECT issue_id FROM issue "
		+ "ORDER BY issue_id DESC "
		+ "LIMIT :cursorSize", nativeQuery = true)
	List<Long> findTop6Issues(@Param("cursorSize") int cursorSize);

	// majorCategory만 설정을 한 경우
	@Query(value = "SELECT issue_id FROM issue "
		+ " WHERE major_category = :majorCategory "
		+ "ORDER BY issue_id DESC "
		+ "LIMIT :cursorSize", nativeQuery = true)
	List<Long> findTop6IssuesByCategory(
		@Param("majorCategory")String majorCategory,
		@Param("cursorSize") int cursorSize
		);

	// majorCategory + page 설정을 한 경우
	@Query(value = "SELECT issue_id FROM issue"
		+ " WHERE major_category = :majorCategory AND issue_id < :page "
		+ "ORDER BY issue_id DESC "
		+ "LIMIT :cursorSize", nativeQuery = true)
	List<Long> findTop6IssuesByPageAndCategory(
		@Param("majorCategory")String majorCategory,
		@Param("page") Long page,
		@Param("cursorSize") int cursorSize
		);

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
        ORDER BY ui1.created_at DESC
        """, nativeQuery = true)
	List<Object[]> findIssuesWithBookmarksOrderByCreatedDate(@Param("issueIds") List<Long> issueIds);

	/* Legacy
	@Query(value = """
    SELECT i.issue_id, i.title, COUNT(ui.issue_id) AS bookmarks 
    FROM (SELECT issue_id, title FROM issue WHERE issue_id = :issueId) i
    LEFT JOIN user_issue ui ON i.issue_id = ui.issue_id
    """, nativeQuery = true)
	List<Object[]> findRelatedIssueWithBookmarks(@Param("issueId") Long issueId);

	 */

	@Query(value = "SELECT iss.issue_id, iss.title, COUNT(iss.issue_id) AS bookmarks " +
		"FROM issue iss " +
		"INNER JOIN (SELECT * FROM user_issue WHERE bookmark = 'yes' AND issue_id = :issueId) usr_iss " +
		"ON iss.issue_id = usr_iss.issue_id " +
		"WHERE iss.issue_id = :issueId", nativeQuery = true)
	List<Object[]> findIssueWithBookmarks(@Param("issueId") Long issueId);


	@Query(value = """
    SELECT chroom.issue_id, COUNT(ch.chat_room_id) AS chats
    FROM chat_room chroom
    LEFT JOIN (
        SELECT chat_room_id, time_stamp 
        FROM chat 
        WHERE time_stamp <= NOW() AND time_stamp >= DATE(NOW())
    ) ch ON chroom.chat_room_id = ch.chat_room_id
    GROUP BY chroom.issue_id
    ORDER BY chats DESC, ch.time_stamp DESC
    LIMIT 5
""", nativeQuery = true)
	List<Object[]> findTop5ActiveIssuesByCountingChats();

	// 즐겨찾기 이미 했나 안했나?
	//@Query(value = "SELECT id, user_id, issue_id, bookmark FROM user_issue WHERE issue_id = :issueId AND user_id = :userId", nativeQuery = true)
	//List<Object[]> findByIssueIdAndUserId(@Param("issueId") Long issueId, @Param("userId") Long userId);

	// 오늘 신규 채팅방 수
	@Query(value = """
        SELECT COUNT(ch.chat_room_id) AS chats 
        FROM (SELECT chat_room_id FROM chat_room WHERE issue_id = :issueId) chroom
        INNER JOIN chat ch ON chroom.chat_room_id = ch.chat_room_id
        WHERE ch.time_stamp <= NOW() AND ch.time_stamp > DATE(NOW())
        """, nativeQuery = true)
	Long countChatsTodayByIssueId(@Param("issueId") Long issueId);


	// 이슈맵전용 쿼리
	@Query(value = """
    SELECT iss2.issue_id, iss2.title, iss2.created_at, iss2.bookmarks, iss3.chat_room_count
    FROM (
        SELECT iss.issue_id, iss.title, iss.created_at, u_iss.bookmarks 
        FROM issue iss  
        LEFT OUTER JOIN (
            SELECT issue_id, SUM(CASE WHEN bookmark = 'yes' THEN 1 ELSE 0 END) AS bookmarks
            FROM user_issue 
            WHERE issue_id IN (:ids)
            GROUP BY issue_id
        ) u_iss
        ON iss.issue_id = u_iss.issue_id
        WHERE iss.issue_id IN (:ids)
    ) iss2
    LEFT OUTER JOIN (
        SELECT issue_id, COUNT(issue_id) AS chat_room_count 
        FROM chat_room
        WHERE issue_id IN (:ids)
        GROUP BY issue_id		
    ) iss3
    ON iss2.issue_id = iss3.issue_id
    ORDER BY iss2.issue_id DESC
    """, nativeQuery = true)
	List<Object[]> findIssueCountingBookmarkAndChatRoom(@Param("ids") List<Long> ids);

}
/* Legacy
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
 */


