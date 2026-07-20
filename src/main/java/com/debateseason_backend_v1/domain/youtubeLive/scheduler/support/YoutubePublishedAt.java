package com.debateseason_backend_v1.domain.youtubeLive.scheduler.support;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * YouTube API의 snippet.publishedAt(RFC3339, 예: "2026-07-20T15:05:19Z")을
 * KST 기준 LocalDateTime으로 변환한다.
 *
 * <p>기존에는 각 수집기가 아래처럼 'Z'를 문자열에서 떼어내고 그대로 파싱했다.
 *
 * <pre>
 * if (createdAt.endsWith("Z")) {
 *     createdAt = createdAt.substring(0, createdAt.length() - 1);
 * }
 * LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
 * </pre>
 *
 * <p>'Z'는 UTC라는 뜻인데 이를 떼어내면 시간대 정보가 사라지고, 남은 값이
 * KST인 것처럼 저장되어 실제보다 9시간 이르게 표시됐다. 서버 타임존과는
 * 무관한 코드 자체의 버그라, 서버가 KST였던 시절에도 동일하게 틀렸다.
 */
public final class YoutubePublishedAt {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	private YoutubePublishedAt() {
	}

	/**
	 * @param publishedAt YouTube가 내려준 RFC3339 문자열 (UTC, 'Z' 접미사)
	 * @return KST 기준 LocalDateTime
	 */
	public static LocalDateTime toKst(String publishedAt) {
		return Instant.parse(publishedAt)
			.atZone(KST)
			.toLocalDateTime();
	}
}
