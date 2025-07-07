package com.debateseason_backend_v1.fixtures.chat;

import com.debateseason_backend_v1.domain.chat.infrastructure.chat.ChatEntity;

import java.util.concurrent.atomic.AtomicLong;

public class ChatEntityFixture {

    static AtomicLong atomicLong = new AtomicLong();

    public static ChatEntity create() {
        Long chatID = atomicLong.incrementAndGet();
        return ChatEntity.builder()
                .id(chatID)
                .build();
    }
}
