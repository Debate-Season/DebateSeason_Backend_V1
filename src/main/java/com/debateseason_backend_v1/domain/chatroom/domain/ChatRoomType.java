package com.debateseason_backend_v1.domain.chatroom.domain;

/**
 * 채팅방의 역할 구분 (v1.3.5 채팅방 스레드 통합).
 *
 * 기존 chat_room 은 "안건 = 방 = 컨테이너" 3역을 겸했다. v1.3.5 에서 이슈당 방을 하나로 합치며
 * 역할을 둘로 쪼갠다.
 *
 * <ul>
 *   <li>{@link #CONTAINER} — 이슈당 1개. 모바일이 보는 채팅방이자 메시지 통합 버킷.</li>
 *   <li>{@link #THREAD}    — 컨테이너 아래 스레드. 기존 방을 강등한 것. 찬반·멤버십·알림을 그대로 보유.</li>
 * </ul>
 *
 * 레거시 행(마이그레이션 전)은 room_type 이 NULL 이며, THREAD 로 해석한다.
 */
public enum ChatRoomType {

	CONTAINER,
	THREAD
}
