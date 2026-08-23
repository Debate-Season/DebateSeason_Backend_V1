package com.debateseason_backend_v1.domain.chatroom.domain;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.debateseason_backend_v1.domain.chat.application.repository.ChatRepository;

/**
 * 대화가 없는 방의 time 문구를 고정한다.
 *
 * 예전에는 대화가 없는 방의 time 이 "" 였다. 클라이언트가 이 줄을 조건 분기 없이 그리기 때문에
 * 홈 "실시간 핫한 토론"(폴백으로 끌어온 방)과 이슈 상세(대화 없는 방이 그냥 섞여 있음) 양쪽에서
 * 빈 줄이 나왔다. 특히 이슈 상세 카드는 높이가 고정이 아니라 제목과 찬반 버튼 사이에 34px 공백이
 * 생겨 레이아웃 버그처럼 보였다. 운영 26개 방 중 6개가 대화 0건이다.
 *
 * 문구를 클라이언트가 만들면 웹·앱이 갈라진다. time 은 서버가 '1일 전 대화' 같은 완성된
 * 표시 문자열을 만드는 필드이므로 빈 값 대체도 같은 계층에 둔다.
 */
@DisplayName("대화 없는 방의 time 문구")
class TimeProcessorNoChatTest {

	private static final Long ROOM_ID = 89L;

	private final ChatRepository chatRepository = mock(ChatRepository.class);
	private final TimeProcessor timeProcessor = new TimeProcessor(chatRepository);

	private void givenLatestChat(LocalDateTime at) {
		given(chatRepository.findMostRecentMessageTimestampByChatRoomId(ROOM_ID))
			.willReturn(Optional.ofNullable(at));
	}

	@Test
	@DisplayName("대화가 하나도 없으면 안내 문구를 준다 (빈 문자열이 아니다)")
	void noChatYieldsPlaceholder() {
		givenLatestChat(null);

		assertThat(timeProcessor.findLastestChatTime(ROOM_ID))
			.isEqualTo("아직 대화가 없어요")
			.isEqualTo(TimeProcessor.NO_CHAT_YET);
	}

	@Test
	@DisplayName("대화가 있으면 문구가 아니라 경과 시간을 그대로 준다")
	void existingChatKeepsElapsedTime() {
		givenLatestChat(LocalDateTime.now().minusDays(3));

		assertThat(timeProcessor.findLastestChatTime(ROOM_ID)).isEqualTo("3일 전 대화");
	}

	@Test
	@DisplayName("null 은 어떤 경우에도 나가지 않는다 (앱 DTO 가 non-nullable)")
	void neverReturnsNull() {
		givenLatestChat(null);
		assertThat(timeProcessor.findLastestChatTime(ROOM_ID)).isNotNull().isNotEmpty();

		givenLatestChat(LocalDateTime.now());
		assertThat(timeProcessor.findLastestChatTime(ROOM_ID)).isNotNull().isNotEmpty();
	}

	@Test
	@DisplayName("빈 문자열은 어디로도 나가지 않는다 (홈·이슈 상세가 같은 메서드를 쓴다)")
	void neverReturnsEmptyString() {
		// 메서드를 둘로 나눠 두면(원본 + 문구 버전) 나중에 원본을 골라 빈 문자열이 조용히
		// 되살아난다. 두 화면 모두 문구를 원하므로 하나로 합쳤다. 그 사실을 고정한다.
		givenLatestChat(null);

		assertThat(timeProcessor.findLastestChatTime(ROOM_ID)).isNotEmpty();
	}
}
