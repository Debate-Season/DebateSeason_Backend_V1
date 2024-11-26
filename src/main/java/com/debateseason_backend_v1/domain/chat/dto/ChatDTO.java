package com.debateseason_backend_v1.domain.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatDTO {
    // 발신자
    private String sender;
    // 소속 커뮤니티
    private String category;
    private String content;
}
