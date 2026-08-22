package com.debateseason_backend_v1.domain.chatroom.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * "실시간 핫한 토론" 랭킹의 집계 창을 정한다.
 *
 * 창은 4단계 캐스케이드다 — 30분 / 8시간 / 24시간 / 72시간. 앞 단계가 5칸을 못 채우면
 * 다음 단계가 남은 칸을 채우고, 72시간에도 대화가 없으면 최근 생성 토론방으로 채운다.
 * 단계 우선이 절대적이다: 30분 창에 1건 있는 방이 8시간 창에 50건 있는 방보다 위다.
 *
 * 네 창 모두 같은 끝점 T 에 앵커된 누적 구간이다 — [T-30분, T), [T-8시간, T) ...
 * T 는 30분 텀블링 경계라 랭킹 전체가 매 정각/30분에 한 번 갱신되고 그 사이엔 고정된다.
 * 배타 구간이 아니라 누적이어도 결과는 같다: 앞 단계에 걸린 방은 이미 뽑혀 나가므로
 * 다음 단계에 남은 방의 카운트는 전부 그보다 옛 구간에서 나온다.
 *
 * 경계를 SQL 의 NOW() 로 잡지 않고 여기서 계산해 파라미터로 넘기는 이유:
 * chat.time_stamp 는 애플리케이션이 LocalDateTime.now() 로 쓴다
 * (JVM 기본 시간대는 DebateSeasonBackendV1Application 에서 Asia/Seoul 로 고정).
 * DB 세션의 NOW() 는 Hikari connection-init-sql 의 SET time_zone='+09:00' 덕에 지금은
 * 우연히 같은 벽시계를 가리키지만, 서로 무관한 두 설정에 정렬이 의존하게 된다.
 * 컬럼을 쓰는 시계와 창을 자르는 시계를 하나로 맞춘다. 순수 함수라 테스트도 된다.
 */
public final class RankingWindow {

	public static final int BUCKET_MINUTES = 30;

	/** 1단계: 직전에 완료된 30분. */
	public static final Duration TIER_30M = Duration.ofMinutes(30);
	/** 2단계: 8시간. */
	public static final Duration TIER_8H = Duration.ofHours(8);
	/** 3단계: 24시간. */
	public static final Duration TIER_24H = Duration.ofHours(24);
	/** 4단계: 72시간. 여기에도 없으면 최근 생성순(5단계)으로 떨어진다. */
	public static final Duration TIER_72H = Duration.ofHours(72);

	private RankingWindow() {
	}

	/**
	 * 네 창이 공유하는 끝점 T (배타). 기준 시각이 속한 30분 버킷의 시작점이다.
	 * 진행 중인 구간은 표본이 불완전하므로 창에 넣지 않는다.
	 */
	public static LocalDateTime bucketEnd(LocalDateTime now) {
		LocalDateTime truncatedToHour = now.truncatedTo(ChronoUnit.HOURS);
		int bucket = now.getMinute() / BUCKET_MINUTES;
		return truncatedToHour.plusMinutes((long)bucket * BUCKET_MINUTES);
	}

	/**
	 * 끝점 T 에서 span 만큼 뒤로 간 시작점 (포함). 창은 [windowStart, bucketEnd) 다.
	 */
	public static LocalDateTime windowStart(LocalDateTime now, Duration span) {
		return bucketEnd(now).minus(span);
	}
}
