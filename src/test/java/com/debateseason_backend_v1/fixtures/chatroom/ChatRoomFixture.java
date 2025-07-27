package com.debateseason_backend_v1.fixtures.chatroom;

import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.fixtures.issue.IssueFixture;

import java.time.LocalDateTime;

public class ChatRoomFixture {

    public static ChatRoom create() {
        return ChatRoom.builder()
                .id(1L)
                .issueEntity(IssueFixture.create())
                .title("title")
                .content("content")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
