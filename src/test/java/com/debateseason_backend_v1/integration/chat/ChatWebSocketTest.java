package com.debateseason_backend_v1.integration.chat;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.common.enums.OpinionType;
import com.debateseason_backend_v1.domain.chat.presentation.dto.request.ChatMessageRequest;
import com.debateseason_backend_v1.domain.chat.presentation.dto.response.ChatMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatWebSocketTest {

    @LocalServerPort
    private int port;
    private WebSocketStompClient stompClient;
    private final BlockingQueue<ChatMessageResponse> messageQueue = new LinkedBlockingQueue<>();
    private String WS_URL;

    @BeforeEach
    void setup(){
        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        this.WS_URL = "ws://localhost:" + port + "/ws-stomp";

        messageQueue.clear();
    }




    @Test
    void 채팅메시지_전송_및_수신_성공() throws Exception {
        System.out.println("USING WS_URL = " + WS_URL);
        //given
        Long roomId = 1L;
        String sender = "testSender";
        String message = "testMessage";
        String userCommunity = "에펨코리아";

        String sendDestination = "/stomp/chat.room."+roomId;
        String subTopic = "/topic/room"+roomId;

        ChatMessageRequest sendMessage = ChatMessageRequest.builder()
                .roomId(roomId)
                .sender(sender)
                .content(message)
                .messageType(MessageType.CHAT)
                .opinionType(OpinionType.AGREE)
                .userCommunity(userCommunity)
                .build();


        //when
        StompSession stompSession = this.stompClient
                .connectAsync(WS_URL, new StompSessionHandlerAdapter() {})
                .get(3, TimeUnit.SECONDS);
        assertNotNull(stompSession, "스톰프 세션 값이 NULL 입니다. 스톰프 세션값 확인 해 주세요");

        stompSession.subscribe(subTopic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.offer((ChatMessageResponse) payload);
            }
        });
        stompSession.send(sendDestination, sendMessage);
        //then
        ChatMessageResponse receivedMessage = messageQueue.poll(3, TimeUnit.SECONDS);
        assertNotNull(receivedMessage);
        assertEquals(receivedMessage.getRoomId(), roomId);
        assertEquals(receivedMessage.getSender(), sender);
        assertEquals(receivedMessage.getContent(), message);
        assertEquals(receivedMessage.getMessageType(), MessageType.CHAT);
        assertEquals(receivedMessage.getOpinionType(), OpinionType.AGREE);
        assertEquals(receivedMessage.getUserCommunity(), userCommunity);
    }




}
