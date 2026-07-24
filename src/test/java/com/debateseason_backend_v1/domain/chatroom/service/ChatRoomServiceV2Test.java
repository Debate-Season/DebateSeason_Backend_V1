package com.debateseason_backend_v1.domain.chatroom.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomType;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.RoomContainerResponse;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.UserChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;

class ChatRoomServiceV2Test {

	@Mock
	private ChatRoomRepository chatRoomRepository;
	@Mock
	private UserChatRoomRepository userChatRoomRepository;
	@Mock
	private IssueJpaRepository issueJpaRepository;

	@InjectMocks
	private ChatRoomServiceV2 chatRoomServiceV2;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	private IssueEntity issue(Long id, String title) {
		return IssueEntity.builder().id(id).title(title).build();
	}

	// findChatRoomAggregates 반환 형태: chat_room_id(0), title(1), content(2), created_at(3), AGREE(4), DISAGREE(5)
	private Object[] aggregateRow(long threadId, String title, long agree, long disagree) {
		return new Object[] {
			BigInteger.valueOf(threadId), title, "content", null,
			BigInteger.valueOf(agree), BigInteger.valueOf(disagree)
		};
	}

	@Nested
	@DisplayName("이슈 컨테이너+스레드 조회")
	class GetRoomByIssue {

		@Test
		@DisplayName("존재하지 않는 이슈면 NOT_FOUND_ISSUE 예외")
		void notFoundIssue() {
			when(issueJpaRepository.findById(99L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> chatRoomServiceV2.getRoomByIssue(99L, null))
				.isInstanceOf(CustomException.class);
		}

		@Test
		@DisplayName("비로그인: 컨테이너 id·스레드 찬반은 채우고 myOpinion 은 모두 NEUTRAL")
		void anonymous() {
			IssueEntity issue = issue(1L, "이슈 제목");
			ChatRoom container = ChatRoom.builder().id(42L).roomType(ChatRoomType.CONTAINER).build();

			when(issueJpaRepository.findById(1L)).thenReturn(Optional.of(issue));
			when(chatRoomRepository.findFirstByIssueEntity_IdAndRoomType(1L, ChatRoomType.CONTAINER))
				.thenReturn(Optional.of(container));
			when(chatRoomRepository.findThreadRoomIdsByIssueId(1L)).thenReturn(List.of(17L, 16L));
			when(chatRoomRepository.findChatRoomAggregates(List.of(17L, 16L))).thenReturn(List.of(
				aggregateRow(17L, "스레드 A", 12L, 8L),
				aggregateRow(16L, "스레드 B", 3L, 1L)
			));

			RoomContainerResponse res = chatRoomServiceV2.getRoomByIssue(1L, null);

			assertThat(res.getContainerRoomId()).isEqualTo(42L);
			assertThat(res.getIssueId()).isEqualTo(1L);
			assertThat(res.getIssueTitle()).isEqualTo("이슈 제목");
			assertThat(res.getThreads()).hasSize(2);
			assertThat(res.getThreads().get(0).getThreadId()).isEqualTo(17L);
			assertThat(res.getThreads().get(0).getAgreeCount()).isEqualTo(12);
			assertThat(res.getThreads().get(0).getDisagreeCount()).isEqualTo(8);
			assertThat(res.getThreads()).allMatch(t -> "NEUTRAL".equals(t.getMyOpinion()));

			// 비로그인은 내 투표 조회를 하지 않는다.
			verify(userChatRoomRepository, never()).findUserChatRoomOpinions(any(), anyList());
		}

		@Test
		@DisplayName("로그인: 투표한 스레드는 내 의견을, 나머지는 NEUTRAL 을 채운다")
		void authenticated() {
			IssueEntity issue = issue(1L, "이슈 제목");
			ChatRoom container = ChatRoom.builder().id(42L).roomType(ChatRoomType.CONTAINER).build();

			when(issueJpaRepository.findById(1L)).thenReturn(Optional.of(issue));
			when(chatRoomRepository.findFirstByIssueEntity_IdAndRoomType(1L, ChatRoomType.CONTAINER))
				.thenReturn(Optional.of(container));
			when(chatRoomRepository.findThreadRoomIdsByIssueId(1L)).thenReturn(List.of(17L, 16L));
			when(chatRoomRepository.findChatRoomAggregates(List.of(17L, 16L))).thenReturn(List.of(
				aggregateRow(17L, "스레드 A", 12L, 8L),
				aggregateRow(16L, "스레드 B", 3L, 1L)
			));
			// 17 번 스레드에만 AGREE 투표
			List<Object[]> myOpinions = List.<Object[]>of(new Object[] {BigInteger.valueOf(17L), "AGREE"});
			when(userChatRoomRepository.findUserChatRoomOpinions(7L, List.of(17L, 16L)))
				.thenReturn(myOpinions);

			RoomContainerResponse res = chatRoomServiceV2.getRoomByIssue(1L, 7L);

			assertThat(res.getThreads().get(0).getMyOpinion()).isEqualTo("AGREE");
			assertThat(res.getThreads().get(1).getMyOpinion()).isEqualTo("NEUTRAL");
		}

		@Test
		@DisplayName("컨테이너가 없으면 containerRoomId 는 null, 스레드가 없으면 빈 목록")
		void noContainerNoThreads() {
			IssueEntity issue = issue(2L, "빈 이슈");

			when(issueJpaRepository.findById(2L)).thenReturn(Optional.of(issue));
			when(chatRoomRepository.findFirstByIssueEntity_IdAndRoomType(2L, ChatRoomType.CONTAINER))
				.thenReturn(Optional.empty());
			when(chatRoomRepository.findThreadRoomIdsByIssueId(2L)).thenReturn(List.of());

			RoomContainerResponse res = chatRoomServiceV2.getRoomByIssue(2L, 7L);

			assertThat(res.getContainerRoomId()).isNull();
			assertThat(res.getThreads()).isEmpty();
			// 스레드가 없으면 집계·투표 조회를 아예 하지 않는다.
			verify(chatRoomRepository, never()).findChatRoomAggregates(anyList());
			verify(userChatRoomRepository, never()).findUserChatRoomOpinions(any(), anyList());
		}
	}
}
