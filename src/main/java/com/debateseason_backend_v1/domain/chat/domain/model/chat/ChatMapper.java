package com.debateseason_backend_v1.domain.chat.domain.model.chat;

import com.debateseason_backend_v1.domain.chat.infrastructure.chat.ChatEntity;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatMessageRequest;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;

import java.time.LocalDateTime;

public class ChatMapper {

    public static ChatEntity toEntity(Chat chat) {
        return ChatEntity.builder()
                .id(chat.getId())
                .chatRoomId(chat.getChatRoomId())
                .userId(chat.getUserId())
                .messageType(chat.getMessageType())
                .content(chat.getContent())
                .sender(chat.getSender())
                .opinionType(chat.getOpinionType())
                .userCommunity(chat.getUserCommunity())
                .timeStamp(chat.getTimeStamp())
                .build();
    }

    public static Chat toDomain(ChatEntity entity){
        return Chat.builder()
                .id(entity.getId())
                .chatRoomId(entity.getChatRoomId())
                .userId(entity.getUserId())
                .messageType(entity.getMessageType())
                .content(entity.getContent())
                .sender(entity.getSender())
                .opinionType(entity.getOpinionType())
                .userCommunity(entity.getUserCommunity())
                .timeStamp(entity.getTimeStamp())
                .build();
    }

}
