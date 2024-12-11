package com.debateseason_backend_v1.domain.chat.controller;

import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.domain.chat.model.ChatMessage;
import com.debateseason_backend_v1.domain.chat.service.ChatServiceV1;

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

    @MessageMapping("chat.room.{roomId}")
    @SendTo("/topic/room{roomId}")
    public ChatMessage chat(@DestinationVariable("roomId") Long roomId, ChatMessage chatMessage) {
        return ChatMessage.builder()
            .roomId(roomId)
            .type(MessageType.CHAT)
            .sender(chatMessage.getSender())
            .content(chatMessage.getContent())
            .opinionType(chatMessage.getOpinionType())
            .userCommunity(chatMessage.getUserCommunity())
            .timeStamp(LocalDateTime.now())
            .build();
    }

    @Operation(summary = "채팅 메시지 전송")
    @MessageMapping("chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage broadcastChatMessage(
            @Parameter(description = "사용자 입장 정보")
            @Payload ChatMessage chatMessage) {

        return ChatMessage.builder()
                .type(MessageType.CHAT)
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
    public ChatMessage announceUserJoin (
            @Parameter(description = "사용자 입장 정보")
            @Payload ChatMessage chatMessage
    ) {
        return ChatMessage.builder()
            .type(MessageType.JOIN)
            .sender(chatMessage.getSender())
            .content(chatMessage.getContent() + " joined!")
            .opinionType(chatMessage.getOpinionType())
            .userCommunity(chatMessage.getUserCommunity())
            .timeStamp(LocalDateTime.now())
            .build();
    }
}
