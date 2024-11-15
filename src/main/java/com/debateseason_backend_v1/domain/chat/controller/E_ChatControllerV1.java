package com.debateseason_backend_v1.domain.chat.controller;

import com.debateseason_backend_v1.domain.chat.model.response.E_ChatListResponse;
import com.debateseason_backend_v1.domain.chat.service.ChatServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Chat API", description = "V1 Chat API")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class E_ChatControllerV1 {

    private final ChatServiceV1 chatServiceV1;

    @Operation(
            summary = "채팅 리스트 가져 옵니다.",
            description = "가장 최근 10개의 채팅 리스트를 가져옵니다."
    )
    @GetMapping("/chat-list")
    public E_ChatListResponse chatList(
            @RequestParam("name") @Valid String to,
            @RequestParam("from") @Valid String from
    ){
        return chatServiceV1.chatList(from, to);
    }

}
