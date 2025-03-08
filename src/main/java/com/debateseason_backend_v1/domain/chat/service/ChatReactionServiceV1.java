package com.debateseason_backend_v1.domain.chat.service;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chat.model.request.ChatReactionRequest;
import com.debateseason_backend_v1.domain.chat.model.response.ChatMessageResponse;
import com.debateseason_backend_v1.domain.repository.ChatReactionRepository;
import com.debateseason_backend_v1.domain.repository.ChatRepository;
import com.debateseason_backend_v1.domain.repository.entity.Chat;
import com.debateseason_backend_v1.domain.repository.entity.ChatReaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatReactionServiceV1 {

    private final ChatRepository chatRepository;
    private final ChatReactionRepository chatReactionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ApiResult<ChatMessageResponse> processReaction(Long chatId, ChatReactionRequest request, Long userId) {
        log.info("반응 처리 서비스: chatId={}, reactionType={}, action={}, userId={}", 
                 chatId, request.getReactionType(), request.getAction(), userId);
        
        // userId가 null인지 먼저 확인
        if (userId == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "userId 가 null 입니다");
        }
        
        // 채팅 메시지 존재 여부 확인
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "메시지를 찾을 수 없습니다"));

        ChatReactionRequest.ReactionType reactionType = request.getReactionType();
        
        if (request.getAction() == ChatReactionRequest.ReactionAction.ADD) {
            addReaction(chat, userId, reactionType);
        } else {
            removeReaction(chat, userId, reactionType);
        }
        
        // 응답 생성
        ChatMessageResponse response = ChatMessageResponse.from(chat, userId, chatReactionRepository);
        
        // WebSocket을 통해 실시간 업데이트 전송
        messagingTemplate.convertAndSend(
                "/topic/chat/rooms/" + chat.getChatRoomId().getId() + "/reactions", 
                response);
        
        return ApiResult.success("이모티콘 반응이 처리되었습니다", response);
    }

    private void addReaction(Chat chat, Long userId, ChatReactionRequest.ReactionType reactionType) {
        log.info("반응 추가: chatId={}, userId={}, reactionType={}", chat.getId(), userId, reactionType);
        
        // userId가 null인지 다시 확인
        if (userId == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "userId 가 null 입니다");
        }
        
        // 이미 같은 타입의 반응이 있는지 확인
        Optional<ChatReaction> existingReaction = chatReactionRepository
                .findByChatIdAndUserIdAndReactionType(chat.getId(), userId, reactionType);
        
        if (existingReaction.isEmpty()) {
            // 새 반응 추가
            ChatReaction reaction = ChatReaction.builder()
                    .chat(chat)
                    .userId(userId)
                    .reactionType(reactionType)
                    .build();
            
            log.info("저장할 반응 객체: chat.id={}, userId={}, reactionType={}", 
                     reaction.getChat().getId(), reaction.getUserId(), reaction.getReactionType());
            
            chatReactionRepository.save(reaction);
            log.info("사용자 id {}가 메시지 id {}에 {} 반응을 추가했습니다", userId, chat.getId(), reactionType);
        } else {
            log.info("이미 존재하는 반응: chatId={}, userId={}, reactionType={}", 
                     chat.getId(), userId, reactionType);
        }
    }

    private void removeReaction(Chat chat, Long userId, ChatReactionRequest.ReactionType reactionType) {
        // 반응 삭제
        chatReactionRepository.deleteByChatIdAndUserIdAndReactionType(chat.getId(), userId, reactionType);
        log.debug("사용자 id {}가 메시지 id {}에서 {} 반응을 제거했습니다", userId, chat.getId(), reactionType);
    }

}
