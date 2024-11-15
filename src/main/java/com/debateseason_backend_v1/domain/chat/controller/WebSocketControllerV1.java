package com.debateseason_backend_v1.domain.chat.controller;


import com.debateseason_backend_v1.domain.chat.model.ChatMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Calendar;

@Tag(name = "WebSocket Chat API", description = "실시간 채팅 API")
@Controller
@RequiredArgsConstructor
public class WebSocketControllerV1 {

    @Operation(summary = "채팅 메시지 전송")
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(
            @Parameter(description = "사용자 입장 정보")
            @Payload ChatMessage chatMessage) {

        return ChatMessage.builder()
                .type(ChatMessage.MessageType.CHAT)
                .sender(chatMessage.getSender())
                .content(chatMessage.getContent())
                .timeStamp(LocalDateTime.now())
                .build();
    }

    @Operation(summary = "사용자 입장")
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(
            @Parameter(description = "사용자 입장 정보")
            @Payload ChatMessage chatMessage
    ) {
        return ChatMessage.builder()
                .type(ChatMessage.MessageType.JOIN)
                .sender(chatMessage.getSender())
                .content(chatMessage.getSender() + " joined!")
                .timeStamp(LocalDateTime.now())
                .build();
    }
}
