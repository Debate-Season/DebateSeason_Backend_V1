package com.debateseason_backend_v1.fixtures.chat;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.common.enums.OpinionType;
import com.debateseason_backend_v1.domain.chat.domain.model.chat.Chat;
import com.debateseason_backend_v1.fixtures.chatroom.ChatRoomFixture;

import java.time.LocalDateTime;

public class ChatFixture {

    public static Chat create() {
        return Chat.builder()
                .id(1L)
                .chatRoomId(ChatRoomFixture.create())
                .userId(1L)
                .messageType(MessageType.CHAT)
                .content("content")
                .sender("sender")
                .opinionType(OpinionType.AGREE)
                .userCommunity("user_community")
                .timeStamp(LocalDateTime.now())
                .build();
    }
}
