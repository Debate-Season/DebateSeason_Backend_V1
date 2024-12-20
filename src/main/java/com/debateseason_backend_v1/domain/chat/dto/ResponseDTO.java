package com.debateseason_backend_v1.domain.chat.dto;

import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ResponseDTO {
    private ChatRoom chatRoom;
    private List<ChatDAO> chatList;
}
