package com.debateseason_backend_v1.domain.chat.application.service;

import com.debateseason_backend_v1.common.enums.MessageType;
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
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomServiceV1;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.chat.application.repository.ReportRepository;
import com.debateseason_backend_v1.domain.chat.domain.model.report.Report;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportStatus;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportTargetType;
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

	// ---------- WebSocket 실시간 메시지 처리 ----------
	
	public ChatMessageResponse processChatMessage(Long roomId, ChatMessageRequest message, SimpMessageHeaderAccessor headerAccessor) {
		// 메시지 유효성 검사
		chatValidate.validateMessageLength(message);
		
		// 채팅방 존재 여부 확인
		ChatRoom chatRoom = chatRoomService.findChatRoomById(roomId);

		// 인증 정보에서 사용자 ID 가져오기
		Long userId = null;
		Principal principal = headerAccessor.getUser();
		if (principal != null) {
			userId = Long.valueOf(principal.getName());
		}

		// 메시지 저장
		ChatEntity chat = ChatEntity.from(message, chatRoom, userId);
		ChatEntity savedchat = chatRepository.save(chat);
		log.debug("채팅 메시지 저장 완료: roomId={}, sender={}", roomId, message.getSender());

		// 빈 반응 정보를 포함한 응답 생성
		return ChatMessageResponse.from(savedchat);
	}

	public ChatMessageResponse processJoinMessage(ChatMessageRequest joinRequest) {
		return ChatMessageResponse.builder()
			.messageType(MessageType.JOIN)
			.sender(joinRequest.getSender())
			.content(joinRequest.getSender() + " joined!")
			.opinionType(joinRequest.getOpinionType())
			.userCommunity(joinRequest.getUserCommunity())
			.timeStamp(LocalDateTime.now())
			.build();
	}

	// ---------- 채팅 메시지 영속성 처리 ----------

	@Transactional
	public void saveMessage(ChatMessageRequest chatMessage) {
		ChatRoom chatRoom = chatRoomService.findChatRoomById(chatMessage.getRoomId());
		ChatEntity chat = convertToEntity(chatMessage, chatRoom);
		
		chatRepository.save(chat);
		log.debug("채팅 메시지 저장 완료: roomId={}, sender={}", 
				chatMessage.getRoomId(), chatMessage.getSender());
	}

	private ChatEntity convertToEntity(ChatMessageRequest message, ChatRoom chatRoom) {
		return ChatEntity.builder()
				.chatRoomId(chatRoom)
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
					// 신고 처리된 메시지인지 확인
					if (acceptedReportChatIds.contains(chatEntity.getId())) {
						// 마스킹된 엔티티 생성
						ChatEntity maskedEntity = ChatEntity.builder()
								.id(chatEntity.getId())
								.chatRoomId(chatEntity.getChatRoomId())
								.userId(chatEntity.getUserId())
								.messageType(chatEntity.getMessageType())
								.content(Chat.REPORTED_MESSAGE_CONTENT)
								.sender(chatEntity.getSender())
								.opinionType(chatEntity.getOpinionType())
								.userCommunity(chatEntity.getUserCommunity())
								.timeStamp(chatEntity.getTimeStamp())
								.build();
						return ChatMessageResponse.from(maskedEntity, userId, chatReactionRepository);
					}
					return ChatMessageResponse.from(chatEntity, userId, chatReactionRepository);
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
	public ApiResult<ChatMessagesResponse> getChatMessagesV2(Long roomId, Long cursor, Long userId) {
		cursor = (cursor == null) ? Long.MAX_VALUE : cursor;

		chatRoomService.findChatRoomById(roomId);

		// 메시지 조회 (최대 20개 + 1개 더 조회하여 hasMore 확인)
		List<ChatEntity> chats = chatRepository.findByRoomIdAndCursor(
				roomId, cursor, PageRequest.of(0, PAGE_SIZE + 1));

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
						// 새로운 최적화된 메서드 사용
						return ChatMessageResponse.fromOptimized(
								maskedEntity, userId, reactionCountsMap, userReactionsMap);
					}
					return ChatMessageResponse.fromOptimized(
							chatEntity, userId, reactionCountsMap, userReactionsMap);
				})
				.toList();

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

	// 헬퍼 메서드: 마스킹된 엔티티 생성 (가독성 위해 분리)
	private ChatEntity createMaskedEntity(ChatEntity original) {
		return ChatEntity.builder()
				.id(original.getId())
				.chatRoomId(original.getChatRoomId())
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
