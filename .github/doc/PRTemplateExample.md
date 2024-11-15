## 📌 변경 사항
WebSocket/STOMP 기반 실시간 채팅 기능 구현 및 채팅방 목록 조회 API 구현

## 🔍 관련 이슈
- [Closes #2 (WebSocket 기본 설정)](https://github.com/Debate-Season/DebateSeason_Backend_V1/issues/2)

## ✨ 작업 내용
1. WebSocket/STOMP 기본 환경 구성  
   - WebSocketConfig 설정

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
@Override
public void configureMessageBroker(MessageBrokerRegistry registry) {
registry.enableSimpleBroker("/sub");
registry.setApplicationDestinationPrefixes("/pub");
}
// ... 추가 설정
}
```
- CORS 설정 및 엔드포인트 매핑
    - StompHandler 인터셉터 구현

2. 채팅 관련 도메인 구현

```java
@Entity
public class ChatRoom {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
private String roomId;
private String name;
// ... 추가 필드
}
```
- ChatMessage 엔티티 구현
    - ChatRoom 엔티티 구현
    - 관련 Repository 구현

3. 채팅방 목록 조회 API 구현

```java
@GetMapping("/api/chat/rooms")
public ResponseEntity<List<ChatRoomDto>> getRooms() {
return ResponseEntity.ok(chatService.findAllRooms());
}
```

4. STOMP 메시지 처리 구현
```java
@MessageMapping("/chat/message")
public void message(ChatMessage message) {
chatService.handleChatMessage(message);
messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
}
```
## ✅ 체크리스트
- [x] 코드 컨벤션을 준수하였나요?
- [x] 불필요한 코드가 남아있지 않나요?
- [x] 테스트는 완료하셨나요?
- [x] WebSocket 연결 테스트를 수행하였나요?

## 📝 리뷰어 참고 사항
1. WebSocket 설정 관련
    - 현재 Simple Broker를 사용 중이며, 추후 RabbitMQ로 전환 예정
    - 핸드쉐이크 시 JWT 토큰 검증 로직 포함

2. 성능 관련
    - 채팅방 목록 조회 시 페이징 처리 적용 (size=20)
    - 채팅 메시지는 MariaDB에 저장하도록 구현

3. 테스트 방법
```md
# WebSocket 연결 테스트
ws://localhost:8080/ws-stomp

# STOMP 구독
/sub/chat/room/{roomId}

# 메시지 발행
/pub/chat/message
```
4. 추후 작업 예정 사항
    - [실시간 메시지 처리 구현 #10](https://github.com/Debate-Season/DebateSeason_Backend_V1/issues/10)
