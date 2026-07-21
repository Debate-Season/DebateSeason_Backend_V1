package com.debateseason_backend_v1.domain.chatroom.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.model.IssueStatus;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;

import jakarta.persistence.EntityManager;

@ActiveProfiles("test")
@DataJpaTest
@DisplayName("이슈/채팅방 작성자·상태")
class ChatRoomOwnershipTest {

	@Autowired
	private EntityManager em;

	@Test
	@DisplayName("status 를 지정하지 않아도 채팅방은 OPEN 으로 저장된다")
	void chatRoomDefaultsToOpen() {
		IssueEntity issue = persistIssue();

		ChatRoom chatRoom = ChatRoom.builder()
			.issueEntity(issue)
			.title("제목")
			.content("내용")
			.createdBy(7L)
			.build();

		em.persist(chatRoom);
		em.flush();

		assertThat(chatRoom.getStatus()).isEqualTo(ChatRoomStatus.OPEN);
		assertThat(chatRoom.getCreatedBy()).isEqualTo(7L);
	}

	@Test
	@DisplayName("status 를 지정하지 않아도 이슈는 PUBLISHED 로 저장된다")
	void issueDefaultsToPublished() {
		IssueEntity issue = persistIssue();

		assertThat(issue.getStatus()).isEqualTo(IssueStatus.PUBLISHED);
	}

	@Test
	@DisplayName("명시한 status 는 기본값으로 덮이지 않는다")
	void explicitStatusIsKept() {
		ChatRoom chatRoom = ChatRoom.builder()
			.issueEntity(persistIssue())
			.title("제목")
			.status(ChatRoomStatus.HIDDEN)
			.build();

		em.persist(chatRoom);
		em.flush();

		assertThat(chatRoom.getStatus()).isEqualTo(ChatRoomStatus.HIDDEN);
	}

	@Test
	@DisplayName("레거시 행처럼 작성자가 없어도 저장된다")
	void createdByIsNullable() {
		ChatRoom chatRoom = ChatRoom.builder()
			.issueEntity(persistIssue())
			.title("레거시")
			.build();

		em.persist(chatRoom);
		em.flush();

		// NULL = 시스템/수동 생성. 소유권 판정 시 ADMIN 만 수정 가능하게 다룬다.
		assertThat(chatRoom.getCreatedBy()).isNull();
	}

	private IssueEntity persistIssue() {
		IssueEntity issue = IssueEntity.builder()
			.title("이슈")
			.majorCategory("사회")
			.build();

		em.persist(issue);
		em.flush();
		return issue;
	}
}
