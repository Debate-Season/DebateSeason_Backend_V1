package com.debateseason_backend_v1.domain.chatroom.infrastructure.entity.processor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.sql.Timestamp;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.debateseason_backend_v1.domain.chatroom.domain.TimeProcessor;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.type.ResponseWithTimeAndOpinion;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;

/**
 * 이슈 상세(GET /api/v1/issue → chatRoomMap[]) 의 time 계약을 고정한다.
 *
 * 홈 카드는 높이 140 고정 + top-align 이라 빈 줄이 아래 여백에 흡수됐지만, 이슈 상세 카드는
 * 높이가 자유라 빈 줄이 제목과 찬반 버튼 사이에 그대로 끼어든다(위아래 여백까지 34px).
 * 게다가 이 화면은 한 이슈의 방을 전부 나열하므로 대화 없는 방이 정상적으로 섞인다 —
 * 운영 26개 방 중 6개가 대화 0건이다.
 */
@DisplayName("이슈 상세 채팅방 목록")
class ChatRoomProcessorIssueDetailTest {

	private static final Long ROOM_ID = 87L;

	private final ChatRoomRepository chatRoomRepository = mock(ChatRoomRepository.class);
	private final TimeProcessor timeProcessor = mock(TimeProcessor.class);
	private final ChatRoomProcessor processor = new ChatRoomProcessor(chatRoomRepository, timeProcessor);

	private void givenRoom(Long roomId) {
		given(chatRoomRepository.findChatRoomAggregates(anyList())).willReturn(List.<Object[]>of(
			new Object[] {
				roomId, "투표용지 부족 사태", "본문",
				// created_at 은 JDBC 가 Timestamp 로 준다 ("2026-06-10 15:01:48.0").
				Timestamp.valueOf("2026-06-10 15:01:48"),
				3L, 5L
			}));
	}

	@Test
	@DisplayName("대화가 없는 방도 time 이 비지 않는다")
	void roomWithoutChatsGetsPlaceholder() {
		givenRoom(ROOM_ID);
		given(timeProcessor.findLastestChatTime(ROOM_ID)).willReturn(TimeProcessor.NO_CHAT_YET);

		assertThat(processor.getChatRoomWithOpinionCount(List.of(ROOM_ID)))
			.singleElement()
			.extracting(ResponseWithTimeAndOpinion::getTime)
			.isEqualTo(TimeProcessor.NO_CHAT_YET);
	}

	@Test
	@DisplayName("대화가 있으면 경과 시간을 그대로 쓴다")
	void roomWithChatsKeepsElapsedTime() {
		givenRoom(ROOM_ID);
		given(timeProcessor.findLastestChatTime(ROOM_ID)).willReturn("3분 전 대화");

		assertThat(processor.getChatRoomWithOpinionCount(List.of(ROOM_ID)))
			.singleElement()
			.extracting(ResponseWithTimeAndOpinion::getTime)
			.isEqualTo("3분 전 대화");
	}

	@Test
	@DisplayName("앱이 파싱하는 필드에 null 이 없다 (createdAt 은 DateTime.parse 로 들어간다)")
	void noNullFieldsInResponse() {
		givenRoom(ROOM_ID);
		given(timeProcessor.findLastestChatTime(ROOM_ID)).willReturn(TimeProcessor.NO_CHAT_YET);

		ResponseWithTimeAndOpinion room = processor.getChatRoomWithOpinionCount(List.of(ROOM_ID)).get(0);

		assertThat(room.getChatRoomId()).isNotNull();
		assertThat(room.getTitle()).isNotNull();
		assertThat(room.getContent()).isNotNull();
		assertThat(room.getCreatedAt()).isNotNull();
		assertThat(room.getOpinion()).isNotNull();
		assertThat(room.getTime()).isNotNull().isNotEmpty();
	}
}
