package com.debateseason_backend_v1.fixtures.chat;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.common.enums.OpinionType;
import com.debateseason_backend_v1.domain.chat.domain.model.chat.Chat;
import com.debateseason_backend_v1.fixtures.chatroom.ChatRoomFixture;

import java.time.LocalDateTime;

public class ChatFixture {

    public static Chat create() {
        return ChatFixture.createWithUserId(1L);
    }

    public static Chat createWithUserId(Long userId) {
        return Chat.builder()
                .id(1L)
                .chatRoomId(ChatRoomFixture.create())
                .userId(userId)
                .messageType(MessageType.CHAT)
                .content("content")
                .sender("sender")
                .opinionType(OpinionType.AGREE)
                .userCommunity("user_community")
                .timeStamp(LocalDateTime.now())
                .build();
    }

    /**
     * 신고된 메시지 테스트용
     */
    public static Chat createMaskReportedChat(Chat chat) {
        return Chat.builder()
                .id(chat.getId())
                .chatRoomId(chat.getChatRoomId())
                .userId(chat.getUserId())
                .messageType(chat.getMessageType())
                .content(Chat.REPORTED_MESSAGE_CONTENT)
                .sender(chat.getSender())
                .opinionType(chat.getOpinionType())
                .userCommunity(chat.getUserCommunity())
                .timeStamp(chat.getTimeStamp())
                .build();
    }

}
