package com.debateseason_backend_v1.integration.chat;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.common.enums.OpinionType;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatMessageRequest;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.response.ChatMessageResponse;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.profile.infrastructure.ProfileEntity;
import com.debateseason_backend_v1.domain.profile.infrastructure.ProfileJpaRepository;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.user.domain.UserRole;
import com.debateseason_backend_v1.security.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatWebSocketTest {

    @LocalServerPort
    private int port;
    private WebSocketStompClient stompClient;
    private final BlockingQueue<ChatMessageResponse> messageQueue = new LinkedBlockingQueue<>();
    private String WS_URL;

    @Autowired
    private IssueJpaRepository issueRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ProfileJpaRepository profileJpaRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_NICKNAME = "testNickname";

    private ChatRoom savedChatRoom;
    private String accessToken;

    @BeforeEach
    void setup(){
        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        this.WS_URL = "ws://localhost:" + port + "/ws-stomp";

        messageQueue.clear();

        // IssueEntity 저장 (ChatRoom의 FK 필수)
        IssueEntity issue = IssueEntity.builder()
                .title("테스트 이슈")
                .majorCategory("정치")
                .build();
        IssueEntity savedIssue = issueRepository.save(issue);

        // ChatRoom 저장 (WebSocket 메시지 처리 시 DB 조회 필수)
        savedChatRoom = ChatRoom.builder()
                .issueEntity(savedIssue)
                .title("테스트 채팅방")
                .content("테스트 채팅방 내용")
                .build();
        savedChatRoom = chatRoomRepository.save(savedChatRoom);

        // 발신자는 서버가 프로필에서 채우므로 프로필과 토큰이 필요하다.
        // nickname이 unique라 테스트 간 재삽입하지 않고 재사용한다.
        profileJpaRepository.findByUserId(TEST_USER_ID)
                .orElseGet(() -> profileJpaRepository.save(
                        ProfileEntity.builder()
                                .userId(TEST_USER_ID)
                                .nickname(TEST_NICKNAME)
                                .build()
                ));
        accessToken = jwtUtil.createAccessToken(TEST_USER_ID, UserRole.USER);
    }

    private StompSession connect(String token) throws Exception {
        StompHeaders connectHeaders = new StompHeaders();
        if (token != null) {
            connectHeaders.add("Authorization", "Bearer " + token);
        }
        return this.stompClient
                .connectAsync(WS_URL, new WebSocketHttpHeaders(), connectHeaders,
                        new StompSessionHandlerAdapter() {})
                .get(3, TimeUnit.SECONDS);
    }

    private void subscribe(StompSession session, String subTopic) {
        session.subscribe(subTopic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.offer((ChatMessageResponse) payload);
            }
        });
    }


    @Test
    void 채팅메시지_전송_및_수신_성공() throws Exception {
        System.out.println("USING WS_URL = " + WS_URL);
        //given
        Long roomId = savedChatRoom.getId();
        String message = "testMessage";
        String userCommunity = "에펨코리아";

        String sendDestination = "/stomp/chat.room."+roomId;
        String subTopic = "/topic/room"+roomId;

        // payload의 sender는 서버가 무시하고 프로필 닉네임으로 덮어쓴다
        ChatMessageRequest sendMessage = ChatMessageRequest.builder()
                .roomId(roomId)
                .sender("spoofedSender")
                .content(message)
                .messageType(MessageType.CHAT)
                .opinionType(OpinionType.AGREE)
                .userCommunity(userCommunity)
                .build();


        //when
        StompSession stompSession = connect(accessToken);
        assertNotNull(stompSession, "스톰프 세션 값이 NULL 입니다. 스톰프 세션값 확인 해 주세요");

        subscribe(stompSession, subTopic);
        stompSession.send(sendDestination, sendMessage);
        //then
        ChatMessageResponse receivedMessage = messageQueue.poll(3, TimeUnit.SECONDS);
        assertNotNull(receivedMessage);
        assertEquals(receivedMessage.getRoomId(), roomId);
        assertEquals(TEST_NICKNAME, receivedMessage.getSender());
        assertEquals(receivedMessage.getContent(), message);
        assertEquals(receivedMessage.getMessageType(), MessageType.CHAT);
        assertEquals(receivedMessage.getOpinionType(), OpinionType.AGREE);
        assertEquals(receivedMessage.getUserCommunity(), userCommunity);
    }

    @Test
    void 인증없는_메시지는_저장되지_않고_브로드캐스트되지_않는다() throws Exception {
        //given
        Long roomId = savedChatRoom.getId();
        String sendDestination = "/stomp/chat.room."+roomId;
        String subTopic = "/topic/room"+roomId;

        ChatMessageRequest sendMessage = ChatMessageRequest.builder()
                .roomId(roomId)
                .content("안녕하세요")
                .messageType(MessageType.CHAT)
                .build();

        //when : 토큰 없이 접속해 발행
        StompSession stompSession = connect(null);
        subscribe(stompSession, subTopic);
        stompSession.send(sendDestination, sendMessage);

        //then : 발신자를 식별할 수 없으므로 아무것도 전파되지 않는다
        assertNull(messageQueue.poll(2, TimeUnit.SECONDS));
    }


}
