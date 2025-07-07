package com.debateseason_backend_v1.fixtures.chat;

import com.debateseason_backend_v1.common.enums.OpinionType;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatMessageRequest;

public class ChatMessageRequestFixture {

    /**
     * 채팅 메시지 요청 객체 생성
     * OpinionType.AGREE
     * MessageType.CHAT
     * @return ChatMessageRequest 객체
     */
    public static ChatMessageRequest createChatMessageRequest() {
        return ChatMessageRequest.of(
                1L,
                "testSender",
                "testContent",
                OpinionType.AGREE,
                "testUserCommunity"
        );
    }
}
