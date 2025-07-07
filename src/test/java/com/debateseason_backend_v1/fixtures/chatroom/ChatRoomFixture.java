package com.debateseason_backend_v1.fixtures.chatroom;

import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.fixtures.issue.IssueFixture;

import java.time.LocalDateTime;

public class ChatRoomFixture {

    public static ChatRoom create() {
        return ChatRoom.builder()
                .id(1L)
                .issue(IssueFixture.create())
                .title("testRoom")
                .content("testContent")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
