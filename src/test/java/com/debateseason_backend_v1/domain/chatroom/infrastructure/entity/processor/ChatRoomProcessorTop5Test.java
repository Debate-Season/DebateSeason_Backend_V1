package com.debateseason_backend_v1.domain.chatroom.infrastructure.entity.processor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.debateseason_backend_v1.domain.chatroom.domain.RankingWindow;
import com.debateseason_backend_v1.domain.chatroom.domain.TimeProcessor;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.messages.Top5BestChatRoom;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;

/**
 * "실시간 핫한 토론" 목록 가공이 결과 개수를 5로 가정하지 않는지 고정한다.
 *
 * 예전에는 for(i=0;i<5;i++) 로 List.get(i) 를 돌아서, 자격 있는 방이 5개 미만이면
 * IndexOutOfBoundsException 이 나 홈 화면 전체가 500 이었다. 30분 창을 쓰면
 * 결과가 5개 미만이거나 0개인 상황이 정상 케이스가 되므로 반드시 크기 기반이어야 한다.
 */
class ChatRoomProcessorTop5Test {

	private final ChatRoomRepository chatRoomRepository = mock(ChatRoomRepository.class);
	private final TimeProcessor timeProcessor = mock(TimeProcessor.class);
	private final ChatRoomProcessor processor = new ChatRoomProcessor(chatRoomRepository, timeProcessor);

	private Object[] row(long issueId, String issueTitle, long roomId, String roomTitle) {
		return new Object[] {issueId, issueTitle, roomId, roomTitle};
	}

	private void givenRows(List<Object[]> rows) {
		given(chatRoomRepository.findTop5ActiveChatRooms(any(), any(), any(), any(), any())).willReturn(rows);
	}

	@Test
	@DisplayName("결과가 5개 미만이어도 터지지 않고 그만큼만 돌려준다")
	void fewerThanFiveRowsDoesNotThrow() {
		givenRows(List.of(
			row(23L, "제9회 지방선거", 87L, "투표용지 부족"),
			row(24L, "FIFA", 88L, "개최지")
		));
		given(timeProcessor.findLastestChatTime(anyLong())).willReturn("3분 전 대화");

		List<Top5BestChatRoom> result = processor.getTop5ActiveRooms();

		assertThat(result).hasSize(2);
		assertThat(result).extracting(Top5BestChatRoom::getDebateId).containsExactly(87L, 88L);
	}

	@Test
	@DisplayName("결과가 0개면 빈 리스트다 (예외를 던지지 않는다)")
	void emptyResultReturnsEmptyList() {
		givenRows(List.of());

		assertThat(processor.getTop5ActiveRooms()).isEmpty();
		verify(timeProcessor, never()).findLastestChatTime(anyLong());
	}

	@Test
	@DisplayName("쿼리 순서를 그대로 유지한다 (웹이 idx+1 로 순위를 붙인다)")
	void queryOrderIsPreserved() {
		givenRows(List.of(
			row(1L, "i1", 10L, "r1"),
			row(2L, "i2", 20L, "r2"),
			row(3L, "i3", 30L, "r3"),
			row(4L, "i4", 40L, "r4"),
			row(5L, "i5", 50L, "r5")
		));
		given(timeProcessor.findLastestChatTime(anyLong())).willReturn("");

		assertThat(processor.getTop5ActiveRooms())
			.extracting(Top5BestChatRoom::getDebateId)
			.containsExactly(10L, 20L, 30L, 40L, 50L);
	}

	@Test
	@DisplayName("대화가 없는 폴백 방의 time 은 빈 문자열로 나간다")
	void fallbackRoomWithoutChatsHasEmptyTime() {
		givenRows(List.<Object[]>of(row(23L, "제9회 지방선거", 87L, "투표용지 부족")));
		given(timeProcessor.findLastestChatTime(87L)).willReturn("");

		assertThat(processor.getTop5ActiveRooms())
			.singleElement()
			.extracting(Top5BestChatRoom::getTime)
			.isEqualTo("");
	}

	@Test
	@DisplayName("네 단계 창을 모두 같은 버킷 경계에 앵커해서 넘긴다")
	void passesAllFourCascadingWindowsAnchoredToOneBucketEnd() {
		givenRows(List.of());

		LocalDateTime before = LocalDateTime.now();
		processor.getTop5ActiveRooms();
		LocalDateTime after = LocalDateTime.now();

		ArgumentCaptor<LocalDateTime> w30 = ArgumentCaptor.forClass(LocalDateTime.class);
		ArgumentCaptor<LocalDateTime> w8 = ArgumentCaptor.forClass(LocalDateTime.class);
		ArgumentCaptor<LocalDateTime> w24 = ArgumentCaptor.forClass(LocalDateTime.class);
		ArgumentCaptor<LocalDateTime> w72 = ArgumentCaptor.forClass(LocalDateTime.class);
		ArgumentCaptor<LocalDateTime> end = ArgumentCaptor.forClass(LocalDateTime.class);
		verify(chatRoomRepository).findTop5ActiveChatRooms(
			w30.capture(), w8.capture(), w24.capture(), w72.capture(), end.capture());

		LocalDateTime bucketEnd = end.getValue();

		// 네 창 모두 같은 끝점에서 각자의 폭만큼 뒤로 간 지점이어야 한다.
		assertThat(w30.getValue()).isEqualTo(bucketEnd.minus(RankingWindow.TIER_30M));
		assertThat(w8.getValue()).isEqualTo(bucketEnd.minus(RankingWindow.TIER_8H));
		assertThat(w24.getValue()).isEqualTo(bucketEnd.minus(RankingWindow.TIER_24H));
		assertThat(w72.getValue()).isEqualTo(bucketEnd.minus(RankingWindow.TIER_72H));

		// 끝점은 완료된 버킷이라 호출 시점보다 앞서 있어야 한다.
		assertThat(bucketEnd).isBeforeOrEqualTo(after);
		// before~after 사이에 버킷 경계가 끼면 둘 중 어느 쪽이어도 맞다(테스트가 흔들리지 않게).
		assertThat(bucketEnd).isIn(
			RankingWindow.bucketEnd(before),
			RankingWindow.bucketEnd(after));
	}
}
