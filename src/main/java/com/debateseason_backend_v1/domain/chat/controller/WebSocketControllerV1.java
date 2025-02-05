package com.debateseason_backend_v1.domain.chat.controller;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.domain.chat.model.request.ChatMessageRequest;

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

    private final ApplicationEventPublisher applicationEventPublisher;

    @MessageMapping("/chat.room.{roomId}")
    @SendTo("/topic/room{roomId}")
    public ChatMessageRequest message(@DestinationVariable Long roomId, @Payload ChatMessageRequest chatMessage) {
        log.debug("채팅 메시지 수신: roomId={}, sender={}", roomId, chatMessage.getSender());
        ChatMessageRequest chatMessage1 = ChatMessageRequest.builder()
                .roomId(roomId)
                .messageType(MessageType.CHAT)
                .sender(chatMessage.getSender())
                .content(chatMessage.getContent())
                .opinionType(chatMessage.getOpinionType())
                .userCommunity(chatMessage.getUserCommunity())
                .timeStamp(LocalDateTime.now())
                .build();

        applicationEventPublisher.publishEvent(chatMessage1);

        return chatMessage1;
    }

    @Operation(summary = "채팅 메시지 전송")
    @MessageMapping("chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessageRequest broadcastChatMessage(
            @Parameter(description = "사용자 입장 정보")
            @Payload ChatMessageRequest chatMessage) {

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
    @MessageMapping("chat.addUser")
    @SendTo("/topic/public")
    public ChatMessageRequest announceUserJoin (
            @Parameter(description = "사용자 입장 정보")
            @Payload ChatMessageRequest chatMessage
    ) {
        return ChatMessageRequest.builder()
            .messageType(MessageType.JOIN)
            .sender(chatMessage.getSender())
            .content(chatMessage.getContent() + " joined!")
            .opinionType(chatMessage.getOpinionType())
            .userCommunity(chatMessage.getUserCommunity())
            .timeStamp(LocalDateTime.now())
            .build();
    }
}
