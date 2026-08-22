package com.debateseason_backend_v1.domain.chatroom.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * "실시간 핫한 토론" 의 집계 창 경계를 고정한다.
 *
 * 네 창(30분/8시간/24시간/72시간)이 모두 같은 30분 버킷 경계 T 에 앵커돼야 한다.
 * 텀블링(고정 구간)이지 슬라이딩이 아니다 — 같은 30분 안에서는 몇 시에 물어봐도
 * 같은 창이 나와야 그 30분 동안 순위가 고정된다.
 */
class RankingWindowTest {

	@Test
	@DisplayName("같은 30분 안에서는 언제 물어도 창이 같다 (슬라이딩이 아니다)")
	void windowIsStableWithinTheBucket() {
		LocalDateTime justAfterBoundary = LocalDateTime.of(2026, 8, 22, 18, 30, 0);
		LocalDateTime nearEndOfBucket = LocalDateTime.of(2026, 8, 22, 18, 59, 59, 999_000_000);

		assertThat(RankingWindow.bucketEnd(justAfterBoundary))
			.isEqualTo(RankingWindow.bucketEnd(nearEndOfBucket))
			.isEqualTo(LocalDateTime.of(2026, 8, 22, 18, 30, 0));

		assertThat(RankingWindow.windowStart(justAfterBoundary, RankingWindow.TIER_72H))
			.isEqualTo(RankingWindow.windowStart(nearEndOfBucket, RankingWindow.TIER_72H));
	}

	@Test
	@DisplayName("경계를 넘으면 창이 정확히 30분 앞으로 간다")
	void windowAdvancesByExactlyOneBucket() {
		LocalDateTime beforeBoundary = LocalDateTime.of(2026, 8, 22, 18, 29, 59);
		LocalDateTime afterBoundary = LocalDateTime.of(2026, 8, 22, 18, 30, 0);

		assertThat(RankingWindow.bucketEnd(beforeBoundary))
			.isEqualTo(LocalDateTime.of(2026, 8, 22, 18, 0, 0));
		assertThat(RankingWindow.bucketEnd(afterBoundary))
			.isEqualTo(LocalDateTime.of(2026, 8, 22, 18, 30, 0));
	}

	@Test
	@DisplayName("네 창이 모두 같은 끝점 T 에 앵커된다 (누적 구간)")
	void allTiersShareTheSameEndpoint() {
		LocalDateTime now = LocalDateTime.of(2026, 8, 22, 18, 45, 12);
		LocalDateTime end = RankingWindow.bucketEnd(now);

		assertThat(end).isEqualTo(LocalDateTime.of(2026, 8, 22, 18, 30, 0));
		// 진행 중인 구간(18:30~)은 표본이 불완전하므로 창에 들어가지 않는다.
		assertThat(end).isBefore(now);

		assertThat(RankingWindow.windowStart(now, RankingWindow.TIER_30M))
			.isEqualTo(LocalDateTime.of(2026, 8, 22, 18, 0, 0));
		assertThat(RankingWindow.windowStart(now, RankingWindow.TIER_8H))
			.isEqualTo(LocalDateTime.of(2026, 8, 22, 10, 30, 0));
		assertThat(RankingWindow.windowStart(now, RankingWindow.TIER_24H))
			.isEqualTo(LocalDateTime.of(2026, 8, 21, 18, 30, 0));
		assertThat(RankingWindow.windowStart(now, RankingWindow.TIER_72H))
			.isEqualTo(LocalDateTime.of(2026, 8, 19, 18, 30, 0));
	}

	@Test
	@DisplayName("창은 넓은 단계일수록 앞 단계를 포함한다 (중첩)")
	void widerTiersContainNarrowerOnes() {
		LocalDateTime now = LocalDateTime.of(2026, 8, 22, 18, 45, 12);

		LocalDateTime t30 = RankingWindow.windowStart(now, RankingWindow.TIER_30M);
		LocalDateTime t8 = RankingWindow.windowStart(now, RankingWindow.TIER_8H);
		LocalDateTime t24 = RankingWindow.windowStart(now, RankingWindow.TIER_24H);
		LocalDateTime t72 = RankingWindow.windowStart(now, RankingWindow.TIER_72H);

		assertThat(t72).isBefore(t24);
		assertThat(t24).isBefore(t8);
		assertThat(t8).isBefore(t30);
	}

	@Test
	@DisplayName("정시 경계에서 날짜/시가 넘어가도 어긋나지 않는다")
	void windowCrossesHourAndDayBoundaries() {
		LocalDateTime justAfterMidnight = LocalDateTime.of(2026, 8, 22, 0, 3, 0);

		assertThat(RankingWindow.bucketEnd(justAfterMidnight))
			.isEqualTo(LocalDateTime.of(2026, 8, 22, 0, 0, 0));
		assertThat(RankingWindow.windowStart(justAfterMidnight, RankingWindow.TIER_30M))
			.isEqualTo(LocalDateTime.of(2026, 8, 21, 23, 30, 0));
		assertThat(RankingWindow.windowStart(justAfterMidnight, RankingWindow.TIER_72H))
			.isEqualTo(LocalDateTime.of(2026, 8, 19, 0, 0, 0));
	}

	@Test
	@DisplayName("초·나노초는 경계에서 잘려나간다")
	void subMinutePrecisionIsTruncated() {
		LocalDateTime messy = LocalDateTime.of(2026, 8, 22, 18, 41, 37, 123_456_789);

		assertThat(RankingWindow.bucketEnd(messy))
			.isEqualTo(LocalDateTime.of(2026, 8, 22, 18, 30, 0));
	}
}
