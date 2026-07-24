package com.debateseason_backend_v1.domain.chatroom.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomType;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.RoomContainerResponse;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.RoomContainerResponse.ThreadSummary;
import com.debateseason_backend_v1.domain.chatroom.model.response.etc.Opinion;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.UserChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * v1.3.5 (Phase 3b-endpoint) — 신 클라이언트의 이슈 진입점.
 *
 * 핵심 통합(스키마·데이터·라우팅)은 이미 라이브 상태이며, 이 서비스는 신 클라이언트가
 * "이슈당 컨테이너 1개 + 스레드 목록"을 발견하도록 읽기 전용으로 조립한다. 추가만(additive)이라
 * 기존/구 앱에 무영향.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ChatRoomServiceV2 {

	private final ChatRoomRepository chatRoomRepository;
	private final UserChatRoomRepository userChatRoomRepository;
	private final IssueJpaRepository issueJpaRepository;

	@Transactional
	public RoomContainerResponse getRoomByIssue(Long issueId, Long userId) {

		// 1. 이슈 확인
		IssueEntity issue = issueJpaRepository.findById(issueId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ISSUE));

		// 2. 컨테이너 방 (없을 수 있음 — 방이 없던 이슈 등)
		Long containerRoomId = chatRoomRepository
			.findFirstByIssueEntity_IdAndRoomType(issueId, ChatRoomType.CONTAINER)
			.map(ChatRoom::getId)
			.orElse(null);

		// 3. 스레드(=옛 방) 목록 + 찬반/내 투표
		List<Long> threadIds = chatRoomRepository.findThreadRoomIdsByIssueId(issueId);
		List<ThreadSummary> threads = threadIds.isEmpty()
			? List.of()
			: buildThreadSummaries(threadIds, userId);

		return RoomContainerResponse.builder()
			.containerRoomId(containerRoomId)
			.issueId(issue.getId())
			.issueTitle(issue.getTitle())
			.threads(threads)
			.build();
	}

	private List<ThreadSummary> buildThreadSummaries(List<Long> threadIds, Long userId) {

		// 로그인 시에만 내 투표를 채운다. 스레드=옛 방 키로 user_chat_room 에 그대로 저장돼 있다.
		Map<Long, String> myOpinions = userId == null
			? Map.of()
			: userChatRoomRepository.findUserChatRoomOpinions(userId, threadIds).stream()
				.collect(Collectors.toMap(
					row -> ((Number)row[0]).longValue(),
					row -> (String)row[1]
				));

		// chat_room_id(0), title(1), content(2), created_at(3), AGREE(4), DISAGREE(5)
		return chatRoomRepository.findChatRoomAggregates(threadIds).stream()
			.map(row -> {
				Long threadId = ((Number)row[0]).longValue();
				return ThreadSummary.builder()
					.threadId(threadId)
					.title((String)row[1])
					.agreeCount(((Number)row[4]).intValue())
					.disagreeCount(((Number)row[5]).intValue())
					.myOpinion(myOpinions.getOrDefault(threadId, Opinion.NEUTRAL.name()))
					.build();
			})
			.toList();
	}
}
