package com.debateseason_backend_v1.domain.chatroom.domain;

/**
 * 채팅방 생명주기.
 *
 * v1.3.2 에서는 컬럼만 도입하고 조회 필터는 걸지 않는다 (IssueStatus 와 동일한 이유).
 * 하드 삭제 대신 DELETED 로 소프트 삭제하고, 신고 누적 시 HIDDEN 으로 내리는 훅을
 * 나중에 붙일 자리를 만들어 둔다.
 *
 * 토론 주제(스레드)는 온톨로지 흐름에서 준비 → 진행 중 → 완료 를 거친다:
 * PREPARING → OPEN → CLOSED. PREPARING·HIDDEN·DELETED 는 목록·랭킹에서 빠진다.
 * CLOSED 의 읽기 전용은 아직 강제하지 않는다 (채팅 발행 경로가 상태를 보지 않음).
 */
public enum ChatRoomStatus {

	PREPARING, // 준비 중 (미노출, 진행 전)
	OPEN,     // 정상 노출, 채팅 가능
	CLOSED,   // 노출되나 읽기 전용
	HIDDEN,   // 미노출 (신고 등)
	DELETED   // 소프트 삭제
}
