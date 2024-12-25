package com.debateseason_backend_v1.domain.chat.controller;

//@Slf4j
//@ActiveProfiles("test")
//@AutoConfigureMockMvc(addFilters = false)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketControllerV1Test {

    /*
    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private BlockingQueue<ChatMessage> blockingQueue;

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
            assertTrue(stompSession.isConnected(), "WebSocket 세션이 연결되지 않았습니다.");
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
                .type(ChatMessage.MessageType.CHAT)
                .content("테스트 메시지")
                .sender("testUser")
                .build();

        stompSession.send("/app/chat.sendMessage", message);

        // 메시지 큐 확인 (큐가 비어있으면 테스트 실패)
        ChatMessage received = blockingQueue.poll(5, TimeUnit.SECONDS);
        assertNotNull(received, "메시지 수신 되지 않음.");
        assertEquals("테스트 메시지", received.getContent());
        assertEquals("testUser", received.getSender());

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

     */
}
