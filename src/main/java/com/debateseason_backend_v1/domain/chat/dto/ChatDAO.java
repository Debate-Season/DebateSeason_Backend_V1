package com.debateseason_backend_v1.domain.chat.dto;

import com.debateseason_backend_v1.domain.chat.model.Chat;
import com.debateseason_backend_v1.domain.chatroom.model.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ChatDAO {
    // 발신자
    private String sender;
    // 소속 커뮤니티
    private String category;
    private String content;
}
