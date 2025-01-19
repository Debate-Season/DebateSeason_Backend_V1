package com.debateseason_backend_v1.domain.chat.model.response;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.common.enums.OpinionType;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {

    private Long id;
    private MessageType messageType;
    private String sender;
    private String content;
    private OpinionType opinionType;
    private String userCommunity;
    private LocalDateTime timeStamp;

}
