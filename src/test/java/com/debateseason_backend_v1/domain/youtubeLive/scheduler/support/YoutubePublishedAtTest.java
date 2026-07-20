package com.debateseason_backend_v1.domain.youtubeLive.scheduler.support;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.TimeZone;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class YoutubePublishedAtTest {

	private final TimeZone original = TimeZone.getDefault();

	@AfterEach
	void restoreTimeZone() {
		TimeZone.setDefault(original);
	}

	@Test
	@DisplayName("UTC publishedAt을 KST(+9시간)로 변환한다")
	void UTC를_KST로_변환한다() {
		// 운영에 실제로 저장됐던 값.
		// 기존 로직은 'Z'를 떼고 15:05:19를 그대로 저장해 9시간 이르게 표시됐다.
		LocalDateTime result = YoutubePublishedAt.toKst("2026-07-20T15:05:19Z");

		assertThat(result).isEqualTo(LocalDateTime.of(2026, 7, 21, 0, 5, 19));
	}

	@Test
	@DisplayName("날짜 경계를 넘어가는 경우에도 정확히 변환한다")
	void 날짜_경계를_넘긴다() {
		// UTC 23:30 -> KST 다음날 08:30
		LocalDateTime result = YoutubePublishedAt.toKst("2026-07-20T23:30:00Z");

		assertThat(result).isEqualTo(LocalDateTime.of(2026, 7, 21, 8, 30, 0));
	}

	@Test
	@DisplayName("소수점 초가 포함된 형식도 파싱한다")
	void 소수점_초를_처리한다() {
		LocalDateTime result = YoutubePublishedAt.toKst("2026-07-20T15:05:19.123Z");

		assertThat(result).isEqualTo(LocalDateTime.of(2026, 7, 21, 0, 5, 19, 123_000_000));
	}

	@Test
	@DisplayName("JVM 기본 타임존이 무엇이든 결과가 같다")
	void JVM_타임존에_영향받지_않는다() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		LocalDateTime asUtc = YoutubePublishedAt.toKst("2026-07-20T15:05:19Z");

		TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
		LocalDateTime asNewYork = YoutubePublishedAt.toKst("2026-07-20T15:05:19Z");

		assertThat(asUtc)
			.isEqualTo(asNewYork)
			.isEqualTo(LocalDateTime.of(2026, 7, 21, 0, 5, 19));
	}
}
