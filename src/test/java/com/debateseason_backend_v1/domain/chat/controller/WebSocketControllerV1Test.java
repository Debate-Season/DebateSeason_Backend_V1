package com.debateseason_backend_v1.domain.chat.controller;


import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.domain.chat.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketControllerV1Test {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private BlockingQueue<ChatMessage> blockingQueue;

    private static final String TEST_CHAT_CONTENT = "테스트 메시지";
    private static final String TEST_CHAT_SENDER = "testUser";

    @BeforeEach
    void setup() {
        blockingQueue = new LinkedBlockingQueue<>();

        // WebSocket 클라이언트 설정
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        // WebSocket 연결
        String wsUrl = "ws://localhost:" + port + "/ws-stomp";
        try {
            log.info("웹소켓에 연결을 시도 중입니다...  : {}", wsUrl);
            stompSession = stompClient.connect(wsUrl, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);
            // 연결 여부 확인
            assertThat(stompSession.isConnected())
                .withFailMessage("WebSocket 세션이 연결되지 않았습니다.")
                .isTrue();
        } catch (Exception e) {
            fail("웹소켓 연결 실패 : " + e.getMessage());
        }
    }

    @Test
    @DisplayName("STOMP  메시지 전송 테스트")
    void sendMessageTest() throws InterruptedException {
        // 구독 핸들러 설정
        StompFrameHandler frameHandler = new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.add((ChatMessage) payload);
            }
        };


        // 구독 수행 후 완료 대기 시간을 500ms
        stompSession.subscribe("/topic/public", frameHandler);
        Thread.sleep(500); // 구독 완료 대기

        // 메시지 생성 및 전송
        ChatMessage message = ChatMessage.builder()
                .type(MessageType.CHAT)
                .content(TEST_CHAT_CONTENT)
                .sender(TEST_CHAT_SENDER)
                .build();

        stompSession.send("/stomp/chat.sendMessage", message);

        // 메시지 큐 확인 (큐가 비어있으면 테스트 실패)
        ChatMessage received = blockingQueue.poll(5, TimeUnit.SECONDS);
        assertThat(received).withFailMessage("메시지 수신 되지 않음").isNotNull();
        assertThat(received.getContent()).withFailMessage("메시지 Content는 \"%s\" 이여야 하나, 테스트에 사용된 Content는 \"%s\" 입니다.",TEST_CHAT_CONTENT,received.getContent()).isEqualTo(TEST_CHAT_CONTENT);
        assertThat(received.getSender()).withFailMessage("Sender는 \"%s\" 이여야 하나, 테스트에 사용된 Sender는 \"%s\" 입니다.", TEST_CHAT_SENDER, received.getSender()).isEqualTo(TEST_CHAT_SENDER);

        // 테스트 후 연결 정리
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
            log.info("stompSession disconnected");
        }
    }


    @AfterEach
    void close() {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
        }
        if (stompClient != null) {
            stompClient.stop();
        }
    }
}
