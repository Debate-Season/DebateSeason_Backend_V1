package com.debateseason_backend_v1.domain.chat.controller;

import com.debateseason_backend_v1.domain.chat.model.ChatMessage;
import com.debateseason_backend_v1.domain.chat.model.response.ChatListResponse;
import com.debateseason_backend_v1.domain.chat.service.ChatServiceV1;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatControllerV1.class)
@AutoConfigureMockMvc(addFilters = false)  // 시큐리티 필터 비활성화
class ChatControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatServiceV1 chatServiceV1;

    @Test
    @DisplayName("채팅 목록 조회 성공 테스트")
    void chatListTest() throws Exception {
        //given
        String from = "user1";
        String to = "user2";

        List<ChatMessage> messageList = Arrays.asList(ChatMessage.builder()
                .type(ChatMessage.MessageType.CHAT)
                .content("안녕하세요")
                .sender(from)
                //.timeStamp(LocalDateTime.now())
                .build());
        ChatListResponse response = ChatListResponse.builder()
                .result(messageList)
                .totalNumberOfMessages(1)
                .build();

        Mockito.when(chatServiceV1.findChatsBetweenUsers(from, to)).thenReturn(response);

        //when & //then
        mockMvc.perform(get("/api/v1/chat/chat-list")
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalNumberOfMessages").value(1))
                .andExpect(jsonPath("$.result[0].content").value("안녕하세요"))
                .andDo(print());


    }
    @Test
    @DisplayName("필수 파라미터 누락 시 실패 테스트")
    void chatListFailure() throws Exception {
        mockMvc.perform(get("/api/v1/chat/chat-list"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

}