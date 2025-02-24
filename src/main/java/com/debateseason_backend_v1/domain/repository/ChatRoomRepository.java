package com.debateseason_backend_v1.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.repository.entity.Issue;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	// 1.Issue로 채팅방 가져오기
	List<ChatRoom> findByIssue(Issue issue);

	Long countByIssue(Issue issue);

	// 2. issue_id와 연관된 chat_room 카운트 하기
	@Query(value = "SELECT COUNT(issue_id) AS COUNT FROM chat_room WHERE issue_id = :issueId",
		nativeQuery = true)
	Long countChatRoomsByIssueId(@Param("issueId") Long issueId);

	// 3. 채팅방 무한 스크롤 페이지네이션(OFFSET 방법). 만약 연속된 채팅방 중에서 중간이 사라진다면 커서 방식을 할 경우 중복된 데이터를 가져와서 OFFSET 방식을 사용함
	// offset = page * (한 페이지가 갖고 있는 요소의 총 수)
	@Query(value = """
    SELECT ch1.issue_id, ch1.chat_room_id, ch1.title, ch1.content, ch1.created_at 
    FROM chat_room ch1
    ,(SELECT issue_id, chat_room_id 
      FROM chat_room
      WHERE issue_id = :issueId
      ORDER BY issue_id, chat_room_id DESC
      LIMIT 2
      OFFSET :page
    ) ch2
    WHERE ch1.chat_room_id = ch2.chat_room_id
    ORDER BY ch2.chat_room_id DESC
""", nativeQuery = true)
	List<Object[]> findChatRoomsByIssueId(@Param("issueId")Long issueid,
		@Param("page")Integer page);
}
