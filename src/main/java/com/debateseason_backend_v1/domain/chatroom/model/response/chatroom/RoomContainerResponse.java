package com.debateseason_backend_v1.domain.chatroom.model.response.chatroom;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * v1.3.5 (Phase 3b-endpoint) — 신 클라이언트의 이슈 진입 응답.
 *
 * 이슈로 진입할 때 "그 이슈의 채팅방(컨테이너) 1개 + 스레드 목록"을 함께 내려준다.
 * 모바일은 {@code containerRoomId} 로 구독/입장하고, 웹은 {@code threads} 를 탭으로 노출한다.
 */
@Getter
@Builder
@Schema(description = "이슈의 컨테이너 채팅방 + 스레드 목록 (v2)")
public class RoomContainerResponse {

	@Schema(description = "모바일이 구독/입장할 컨테이너 방 id. 컨테이너가 없으면 null.", example = "42")
	private Long containerRoomId;

	@Schema(description = "이슈 id", example = "1")
	private Long issueId;

	@Schema(description = "이슈 제목")
	private String issueTitle;

	@Schema(description = "이슈 하위 스레드(=기존 방) 목록")
	private List<ThreadSummary> threads;

	@Getter
	@Builder
	@Schema(description = "스레드 요약 — 찬반 집계와 내 투표 포함")
	public static class ThreadSummary {

		@Schema(description = "스레드 id (= 기존 채팅방 id)", example = "17")
		private Long threadId;

		@Schema(description = "스레드 제목")
		private String title;

		@Schema(description = "찬성 수", example = "12")
		private int agreeCount;

		@Schema(description = "반대 수", example = "8")
		private int disagreeCount;

		@Schema(description = "내 투표 (AGREE/DISAGREE/NEUTRAL). 비로그인/미투표는 NEUTRAL.", example = "NEUTRAL")
		private String myOpinion;
	}
}
