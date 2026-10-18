package com.debateseason_backend_v1.domain.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomStatus;
import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomType;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.issue.model.IssueStatus;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;

import jakarta.persistence.EntityManager;

/**
 * 롤백 결과(ARCHIVED 이슈·HIDDEN 토론 주제)가 홈 랭킹에 새지 않는지 고정한다.
 * 추천·랭킹은 목록 필터를 빠뜨리기 가장 쉬운 경로다. DORMANT 는 분류일 뿐이라 그대로 나와야 한다.
 */
@ActiveProfiles("test")
@DataJpaTest
@DisplayName("숨긴 콘텐츠와 홈 랭킹")
class HiddenContentRankingTest {

	@Autowired
	private EntityManager em;
	@Autowired
	private ChatRoomRepository chatRoomRepository;
	@Autowired
	private IssueJpaRepository issueJpaRepository;

	private IssueEntity issue(IssueStatus status) {
		IssueEntity issue = IssueEntity.builder().title("이슈 " + status).majorCategory("정치").status(status).build();
		em.persist(issue);
		return issue;
	}

	private Long thread(IssueEntity issue, ChatRoomStatus status) {
		ChatRoom room = ChatRoom.builder()
			.issueEntity(issue).title("주제").roomType(ChatRoomType.THREAD).status(status).build();
		em.persist(room);
		em.flush();
		// 랭킹 꼬리 정렬이 created_at 을 쓴다. 슬라이스 테스트에서는 auditing 이 꺼져 있을 수 있다.
		em.createNativeQuery("UPDATE chat_room SET created_at = CURRENT_TIMESTAMP WHERE chat_room_id = :id")
			.setParameter("id", room.getId()).executeUpdate();
		return room.getId();
	}

	@Test
	@DisplayName("실시간 핫한 토론: HIDDEN 방과 ARCHIVED 이슈의 방은 빠지고 DORMANT 이슈의 방은 남는다")
	void top5ActiveRooms() {
		Long open = thread(issue(IssueStatus.PUBLISHED), ChatRoomStatus.OPEN);
		Long dormant = thread(issue(IssueStatus.DORMANT), ChatRoomStatus.OPEN);
		Long hidden = thread(issue(IssueStatus.PUBLISHED), ChatRoomStatus.HIDDEN);
		Long archived = thread(issue(IssueStatus.ARCHIVED), ChatRoomStatus.OPEN);

		LocalDateTime now = LocalDateTime.now();
		List<Long> roomIds = chatRoomRepository.findTop5ActiveChatRooms(
				now.minusMinutes(30), now.minusHours(8), now.minusHours(24), now.minusHours(72), now.plusMinutes(1))
			.stream().map(row -> ((Number)row[2]).longValue()).toList();

		assertThat(roomIds).contains(open, dormant).doesNotContain(hidden, archived);
	}

	@Test
	@DisplayName("이슈맵 페이지: ARCHIVED 만 빠진다")
	void issueMapPages() {
		Long published = issue(IssueStatus.PUBLISHED).getId();
		Long dormant = issue(IssueStatus.DORMANT).getId();
		Long archived = issue(IssueStatus.ARCHIVED).getId();
		em.flush();

		assertThat(issueJpaRepository.findTop6Issues()).contains(published, dormant).doesNotContain(archived);
		assertThat(issueJpaRepository.findTop6IssuesByCategory("정치")).doesNotContain(archived);
		assertThat(issueJpaRepository.findTop6IssuesByPage(Long.MAX_VALUE)).doesNotContain(archived);
	}
}
