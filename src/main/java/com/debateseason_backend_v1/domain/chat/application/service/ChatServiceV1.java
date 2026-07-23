package com.debateseason_backend_v1.domain.chat.application.service;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chat.application.repository.ChatReactionRepository;
import com.debateseason_backend_v1.domain.chat.application.repository.ChatRepository;
import com.debateseason_backend_v1.domain.chat.domain.model.chat.Chat;
import com.debateseason_backend_v1.domain.chat.infrastructure.chat.ChatEntity;
import com.debateseason_backend_v1.domain.chat.infrastructure.report.ReportEntity;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatMessageRequest;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatReactionRequest;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.response.ChatMessageResponse;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.response.ChatMessagesResponse;
import com.debateseason_backend_v1.domain.chat.validation.ChatValidate;
import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomType;
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomServiceV1;
import com.debateseason_backend_v1.domain.notification.application.service.NotificationServiceV1;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.chat.application.repository.ReportRepository;
import com.debateseason_backend_v1.domain.chat.domain.model.report.Report;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportStatus;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportTargetType;
import com.debateseason_backend_v1.domain.profile.infrastructure.ProfileEntity;
import com.debateseason_backend_v1.domain.profile.infrastructure.ProfileJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceV1 {

	private static final int PAGE_SIZE = 20;
	
	private final ChatRepository chatRepository;
	private final ChatRoomServiceV1 chatRoomService;
	private final ChatValidate chatValidate;
	private final ChatReactionRepository chatReactionRepository;
	private final ReportRepository reportRepository;
    private final NotificationServiceV1 notificationService;
	private final ProfileJpaRepository profileJpaRepository;

	// ---------- WebSocket 실시간 메시지 처리 ----------
	
	public ChatMessageResponse processChatMessage(Long roomId, ChatMessageRequest message, SimpMessageHeaderAccessor headerAccessor) {
		// 메시지 유효성 검사
		chatValidate.validateMessageLength(message);
		
		// 채팅방 존재 여부 확인 + 스레드 통합 라우팅
		// 구 앱이 옛 방(=스레드)으로 보내면 컨테이너에 저장하고 이 방을 thread_id 로 태그한다.
		ChatRoom target = chatRoomService.findChatRoomById(roomId);
		RoutedTarget routed = resolveRouting(target, message.getThreadId());
		message.setThreadId(routed.threadId());

		// 인증 정보에서 사용자 ID 가져오기 (발신자를 식별할 수 없는 메시지는 저장하지 않는다)
		Long userId = resolveUserId(headerAccessor);

		// 발신자는 서버가 프로필에서 채운다. 클라이언트가 보낸 sender 값은 신뢰하지 않는다.
		ProfileEntity profile = profileJpaRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PROFILE));
		message.setSender(profile.getNickname());

		// 메시지 저장 (chat_room_id = 컨테이너, thread_id = 스레드)
		ChatEntity chat = ChatEntity.from(message, routed.container(), userId);
		ChatEntity savedchat = chatRepository.save(chat);

		// 프로필 색상 조회
		String profileColor = profile.getProfileImage();

		// 브로드캐스트 roomId 는 클라이언트가 구독 중인 주소(원래 roomId)로 유지 → 구 앱 실시간 호환
		return ChatMessageResponse.from(savedchat, profileColor, roomId);
	}

	// 스레드 통합 라우팅: 발신 대상 방으로부터 (저장할 컨테이너 방, thread_id) 를 결정한다.
	// 이관 전(레거시, room_type=NULL)에는 대상 방을 그대로 반환하므로 기존 동작과 동일하다.
	private RoutedTarget resolveRouting(ChatRoom target, Long requestedThreadId) {
		if (target.getRoomType() == ChatRoomType.THREAD && target.getContainerRoomId() != null) {
			// 옛 방(스레드)로 온 발신 → 컨테이너에 저장, 이 방이 곧 스레드
			ChatRoom container = chatRoomService.findChatRoomById(target.getContainerRoomId());
			return new RoutedTarget(container, target.getId());
		}
		// CONTAINER 또는 레거시: 그대로. threadId 는 클라이언트 지정값(없으면 null = 전체)
		return new RoutedTarget(target, requestedThreadId);
	}

	private record RoutedTarget(ChatRoom container, Long threadId) {
	}

	private Long resolveUserId(SimpMessageHeaderAccessor headerAccessor) {
		Principal principal = headerAccessor.getUser();
		if (principal == null) {
			throw new CustomException(ErrorCode.MISSING_ACCESS_TOKEN);
		}
		try {
			return Long.valueOf(principal.getName());
		} catch (NumberFormatException e) {
			throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
		}
	}

	// ---------- 채팅 메시지 영속성 처리 ----------

	@Transactional
	public void saveMessage(ChatMessageRequest chatMessage) {
		ChatRoom target = chatRoomService.findChatRoomById(chatMessage.getRoomId());
		RoutedTarget routed = resolveRouting(target, chatMessage.getThreadId());
		chatMessage.setThreadId(routed.threadId());
		ChatEntity chat = convertToEntity(chatMessage, routed.container());

		chatRepository.save(chat);
		log.debug("채팅 메시지 저장 완료: roomId={}, sender={}",
				chatMessage.getRoomId(), chatMessage.getSender());
	}

	private ChatEntity convertToEntity(ChatMessageRequest message, ChatRoom chatRoom) {
		return ChatEntity.builder()
				.chatRoomId(chatRoom)
				.threadId(message.getThreadId())
				.sender(message.getSender())
				.content(message.getContent())
				.messageType(message.getMessageType())
				.opinionType(message.getOpinionType())
				.userCommunity(message.getUserCommunity())
				.timeStamp(message.getTimeStamp())
				.build();
	}

	// ---------- 채팅 메시지 조회 ----------
	@Transactional(readOnly = true)
	public ApiResult<ChatMessagesResponse> getChatMessages(Long roomId, Long cursor, Long userId) {
		cursor = (cursor == null) ? Long.MAX_VALUE : cursor;
		
		// 채팅방 존재 여부 확인
		chatRoomService.findChatRoomById(roomId);
		
		// 메시지 조회 (최대 20개 + 1개 더 조회하여 hasMore 확인)
		List<ChatEntity> chats = chatRepository.findByRoomIdAndCursor(
				roomId, cursor, PageRequest.of(0, PAGE_SIZE + 1));
		
		boolean hasMore = chats.size() > PAGE_SIZE;
		List<ChatEntity> displayChats = hasMore ? chats.subList(0, PAGE_SIZE) : chats;
		
		// 프로필 색상 배치 조회
		List<Long> userIds = displayChats.stream()
				.map(ChatEntity::getUserId)
				.filter(Objects::nonNull)
				.distinct()
				.toList();
		Map<Long, String> profileColorMap = profileJpaRepository.findByUserIdIn(userIds).stream()
				.collect(Collectors.toMap(ProfileEntity::getUserId, ProfileEntity::getProfileImage, (a, b) -> a));

		// 신고된 메시지 목록 조회
		Set<Long> acceptedReportChatIds = reportRepository.findByTargetTypeAndStatus(
				ReportTargetType.CHAT, ReportStatus.ACCEPTED)
				.stream()
				.map(entity -> entity.getTargetId())
				.collect(Collectors.toSet());

		// 디버깅 로그 추가
		log.info("승인된 신고 메시지 ID 목록: {}", acceptedReportChatIds);

		// 응답 생성 - 신고된 메시지 마스킹 적용
		List<ChatMessageResponse> messageResponses = displayChats.stream()
				.map(chatEntity -> {
					String profileColor = chatEntity.getUserId() != null
							? profileColorMap.getOrDefault(chatEntity.getUserId(), null)
							: null;
					// 신고 처리된 메시지인지 확인
					if (acceptedReportChatIds.contains(chatEntity.getId())) {
						// 마스킹된 엔티티 생성
						ChatEntity maskedEntity = ChatEntity.builder()
								.id(chatEntity.getId())
								.chatRoomId(chatEntity.getChatRoomId())
								.threadId(chatEntity.getThreadId())
								.userId(chatEntity.getUserId())
								.messageType(chatEntity.getMessageType())
								.content(Chat.REPORTED_MESSAGE_CONTENT)
								.sender(chatEntity.getSender())
								.opinionType(chatEntity.getOpinionType())
								.userCommunity(chatEntity.getUserCommunity())
								.timeStamp(chatEntity.getTimeStamp())
								.build();
						return ChatMessageResponse.from(maskedEntity, userId, chatReactionRepository, profileColor);
					}
					return ChatMessageResponse.from(chatEntity, userId, chatReactionRepository, profileColor);
				})
				.collect(Collectors.toList());
		
		// 기존 페이지네이션 코드 유지
		String nextCursor = null;
		if (!displayChats.isEmpty()) {
			nextCursor = String.valueOf(displayChats.get(displayChats.size() - 1).getId());
		}
		
		int totalCount = chatRepository.countByRoomId(roomId);
		
		ChatMessagesResponse response = ChatMessagesResponse.builder()
				.items(messageResponses)
				.hasMore(hasMore)
				.nextCursor(nextCursor)
				.totalCount(totalCount)
				.build();
		
		return ApiResult.success("채팅 메시지를 성공적으로 조회했습니다.", response);
	}

	// ---------- 채팅 메시지 조회 v2 배치쿼리 ----------
	@Transactional(readOnly = true)
	public ApiResult<ChatMessagesResponse> getChatMessagesV2(Long roomId, Long threadId, Long cursor, Long userId) {
		cursor = (cursor == null) ? Long.MAX_VALUE : cursor;

		ChatRoom target = chatRoomService.findChatRoomById(roomId);

		// 조회 라우팅 (최대 20개 + 1개 더 조회하여 hasMore 확인)
		// - THREAD(옛 방): 구 앱 히스토리 = thread_id 필터 (메시지는 컨테이너에 저장돼 있음)
		// - CONTAINER + threadId: 웹 스레드 탭
		// - CONTAINER/레거시: 전체 스트림 (기존 동작)
		List<ChatEntity> chats;
		int totalCount;
		if (target.getRoomType() == ChatRoomType.THREAD) {
			chats = chatRepository.findByThreadIdAndCursor(roomId, cursor, PageRequest.of(0, PAGE_SIZE + 1));
			totalCount = chatRepository.countByThreadId(roomId);
		} else if (threadId != null) {
			chats = chatRepository.findByRoomIdAndThreadIdAndCursor(roomId, threadId, cursor, PageRequest.of(0, PAGE_SIZE + 1));
			totalCount = chatRepository.countByRoomIdAndThreadId(roomId, threadId);
		} else {
			chats = chatRepository.findByRoomIdAndCursor(roomId, cursor, PageRequest.of(0, PAGE_SIZE + 1));
			totalCount = chatRepository.countByRoomId(roomId);
		}

		boolean hasMore = chats.size() > PAGE_SIZE;

		List<ChatEntity> displayChats = hasMore ? chats.subList(0, PAGE_SIZE) : chats;

		//조회된 메시지 채팅 ID 추출
		List<Long> chatIds = displayChats.stream()
				.map(ChatEntity::getId)
				.toList();

		//chatIds 메시지들 반응 수 조회
		Map<Long, Map<ChatReactionRequest.ReactionType, Integer>> reactionCountsMap = chatReactionRepository.findReactionCountsByChatIdsIn(chatIds);
		//chatIds 메시지들 사용자 반응
		Map<Long, Set<ChatReactionRequest.ReactionType>> userReactionsMap = chatReactionRepository.findUserReactionsByChatIdsIn(chatIds, userId);

		// 프로필 색상 배치 조회
		List<Long> userIds = displayChats.stream()
				.map(ChatEntity::getUserId)
				.filter(Objects::nonNull)
				.distinct()
				.toList();
		Map<Long, String> profileColorMap = profileJpaRepository.findByUserIdIn(userIds).stream()
				.collect(Collectors.toMap(ProfileEntity::getUserId, ProfileEntity::getProfileImage, (a, b) -> a));

		//신고 메시지 처리 로직
		Set<Long> acceptedReportChatIds = reportRepository.findByTargetTypeAndStatus(
						ReportTargetType.CHAT, ReportStatus.ACCEPTED)
				.stream()
				.map(ReportEntity::getTargetId)
				.collect(Collectors.toSet());

		// 디버깅 로그 추가
		log.debug("@@ 승인된 신고 메시지 ID 목록: {}", acceptedReportChatIds);

		List<ChatMessageResponse> messageResponses = displayChats.stream()
				.map(chatEntity -> {
					if (acceptedReportChatIds.contains(chatEntity.getId())) {
						ChatEntity maskedEntity = createMaskedEntity(chatEntity);
						return ChatMessageResponse.fromOptimized(
								maskedEntity, userId, reactionCountsMap, userReactionsMap, profileColorMap);
					}
					return ChatMessageResponse.fromOptimized(
							chatEntity, userId, reactionCountsMap, userReactionsMap, profileColorMap);
				})
				.toList();

		// 기존 페이지네이션 코드 유지
		String nextCursor = null;
		if (!displayChats.isEmpty()) {
			nextCursor = String.valueOf(displayChats.get(displayChats.size() - 1).getId());
		}

		ChatMessagesResponse response = ChatMessagesResponse.builder()
				.items(messageResponses)
				.hasMore(hasMore)
				.nextCursor(nextCursor)
				.totalCount(totalCount)
				.build();

		return ApiResult.success("채팅 메시지를 성공적으로 조회했습니다.", response);

	}

	// 헬퍼 메서드: 마스킹된 엔티티 생성 (가독성 위해 분리)
	private ChatEntity createMaskedEntity(ChatEntity original) {
		return ChatEntity.builder()
				.id(original.getId())
				.chatRoomId(original.getChatRoomId())
				.threadId(original.getThreadId())
				.userId(original.getUserId())
				.messageType(original.getMessageType())
				.content(Chat.REPORTED_MESSAGE_CONTENT)  // 신고된 메시지 내용으로 대체
				.sender(original.getSender())
				.opinionType(original.getOpinionType())
				.userCommunity(original.getUserCommunity())
				.timeStamp(original.getTimeStamp())
				.build();
	}

}
