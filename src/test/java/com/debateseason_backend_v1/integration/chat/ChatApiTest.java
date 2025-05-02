package com.debateseason_backend_v1.integration.chat;


import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.common.enums.OpinionType;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chat.model.response.ChatMessagesResponse;
import com.debateseason_backend_v1.domain.repository.ChatRepository;
import com.debateseason_backend_v1.domain.repository.entity.Chat;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatRepository chatRepository;

    private String baseUrl;


    @BeforeEach
    void setup(){
        this.baseUrl = "http://localhost:" + port + "/api/v1/chat";

    }

    @Test
    void 보안설정_확인() {
        String[] profiles = context.getEnvironment().getActiveProfiles();
        System.out.println("활성 프로필: " + Arrays.toString(profiles));

        SecurityFilterChain filterChain = context.getBean(SecurityFilterChain.class);
        System.out.println("보안 필터체인: " + filterChain);
    }



    /// 채팅방 메시지 조회 API_URL = /api/v1/chat/rooms/{room식별자}/messages
    @Test
    void 채팅메시지_조회_성공() throws Exception {
        //given
        Long roomId = 1L;
        Long userId = 99L;

        int messageCount = 50;
        List<Chat> chats = prepareTestChatMessages(roomId, messageCount);
        String apiUrl = baseUrl + "/rooms/" + roomId + "/messages";
        System.out.println("@@@ apiUrl: " + apiUrl);
        //when
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jwtUtil.createAccessToken(userId));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = testRestTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                entity,
                String.class);

        //then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResult<ChatMessagesResponse> result = objectMapper.readValue(
                response.getBody(),
                new TypeReference<ApiResult<ChatMessagesResponse>>() {}

        );

        assertNotNull(result.getData());
        ChatMessagesResponse resultData = result.getData();
        assertEquals(resultData.getTotalCount(), messageCount);

    }


    @Autowired private JwtUtil jwtUtil;
    private String createTestJwt(Long userId) {
        return jwtUtil.createAccessToken(userId);
    }


    @Autowired
    private ApplicationContext context;
    /**
     * 테스트에 필요한 채팅 메시지를 데이터베이스에 준비하는 메서드
     * @param count count만큼 채팅메시지가 생성 된다.
     * @param roomId 채팅 메시지를 생성할 roomId
     */
    private List<Chat> prepareTestChatMessages(Long roomId, int count) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(roomId);

        List<Chat> savedChats = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Chat chat = Chat.builder()
                    .chatRoomId(chatRoom)
                    .content("테스트 메시지 " + i)
                    .sender("테스트 사용자")
                    .messageType(MessageType.CHAT)
                    .opinionType(OpinionType.NEUTRAL)
                    .userCommunity("테스트 커뮤니티")
                    .timeStamp(LocalDateTime.now().minusMinutes(count - i)) // 시간순 정렬을 위해
                    .build();

            savedChats.add(chatRepository.save(chat));
        }

        return savedChats;
    }
}
