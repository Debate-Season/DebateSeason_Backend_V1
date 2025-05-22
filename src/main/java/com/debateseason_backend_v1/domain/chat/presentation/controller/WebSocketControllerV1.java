package com.debateseason_backend_v1.domain.chat.presentation.controller;

import java.time.LocalDateTime;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.response.ChatMessageErrorResponse;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.response.ChatMessageResponse;
import com.debateseason_backend_v1.domain.chat.application.service.ChatServiceV1;
import com.debateseason_backend_v1.domain.chat.validation.ChatValidate;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatMessageRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "WebSocket Chat API", description = "실시간 채팅 API")
@Controller
@RequiredArgsConstructor
public class WebSocketControllerV1 {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatValidate chatValidate;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ChatServiceV1 chatService;

    @MessageMapping("/chat.room.{roomId}")
    @SendTo("/topic/room{roomId}")
    public ChatMessageResponse handleChatMessage(
        @DestinationVariable Long roomId,
        @Valid @Payload ChatMessageRequest message,
        @Header("simpSessionId") String sessionId,
        SimpMessageHeaderAccessor headerAccessor
    ) {
        try {
            return chatService.processChatMessage(roomId, message,headerAccessor);

        } catch (CustomException e) {
            throw e; // MessageExceptionHandler 로 전달
        }
    }
    //TODO:  메시지 핸들러가 컨트롤러 클래스에 같이 있는게 맞는건지 고민.. 일다는 @SendToUser 로 라우팅 해서 컨트롤러클래스에 같이 작성
    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser("/queue/errors")
    public ChatMessageErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
            .getFieldError()
            .getDefaultMessage();
        
        log.error("메시지 유효성 검사 실패: {}", errorMessage);
        
        return ChatMessageErrorResponse.builder()
            .messageType(MessageType.ERROR)
            .message(errorMessage)
            .build();
    }

    @MessageExceptionHandler(CustomException.class)
    @SendToUser("/queue/errors")
    public ChatMessageErrorResponse handleCustomException(CustomException ex) {
        log.error("메시지 처리 중 오류 발생: {}", ex.getMessage());
        
        return ChatMessageErrorResponse.builder()
                .messageType(MessageType.ERROR)
                .message(ex.getMessage())
                .build();
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public ChatMessageErrorResponse handleException(Exception ex) {
        log.error("예상치 못한 오류 발생: {}", ex.getMessage());
        
        return ChatMessageErrorResponse.builder()
            .messageType(MessageType.ERROR)
            .message("메시지 처리 중 오류가 발생 했습니다")
            .build();
    }

    @Operation(summary = "채팅 메시지 전송")
    @MessageMapping("chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessageRequest broadcastChatMessage(
            @Parameter(description = "사용자 입장 정보")
            @Payload @Valid ChatMessageRequest chatMessage) {

        return ChatMessageRequest.builder()
                .messageType(MessageType.CHAT)
                .sender(chatMessage.getSender())
                .content(chatMessage.getContent())
                .opinionType(chatMessage.getOpinionType())
                .userCommunity(chatMessage.getUserCommunity())
                .timeStamp(LocalDateTime.now())
                .build();
    }

    @Operation(summary = "사용자 입장")
    @MessageMapping("/chat.join")
    @SendTo("/topic/public")
    public ChatMessageResponse handleUserJoin(@Valid @Payload ChatMessageRequest joinRequest) {
        return chatService.processJoinMessage(joinRequest);
    }
}
