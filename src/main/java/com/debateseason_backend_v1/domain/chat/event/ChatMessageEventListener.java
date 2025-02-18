package com.debateseason_backend_v1.domain.chat.event;

import com.debateseason_backend_v1.domain.chat.model.request.ChatMessageRequest;
import com.debateseason_backend_v1.domain.chat.service.ChatServiceV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageEventListener {

    private final ChatServiceV1 chatService;

    @Async
    @EventListener
    public void handleChatMessageEvent(ChatMessageRequest chatMessage) {
        try {
            chatService.saveMessage(chatMessage);
        }catch (Exception e) {
            log.error("채팅 메시지 저장 실패: {}", e.getMessage(), e);
        }
    }
}
