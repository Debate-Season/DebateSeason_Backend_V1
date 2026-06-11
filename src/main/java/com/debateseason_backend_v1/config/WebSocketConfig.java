package com.debateseason_backend_v1.config;

import com.debateseason_backend_v1.security.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.support.MessageBuilder;

import java.security.Principal;
import java.util.List;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.MimeTypeUtils;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final int MAX_MESSAGE_LENGTH = 500;  // 최대 메시지 길이
    private final ObjectMapper objectMapper;  // JSON 파싱을 위한 ObjectMapper 주입
    private final JwtUtil jwtUtil;

    @Value("${stomp-connect-url}")
    private String stompConnectUrl;

    // true 이면 토큰 없는/유효하지 않은 STOMP CONNECT 를 거부한다.
    // 기존(미업데이트) 앱은 CONNECT 에 토큰을 싣지 않으므로, 앱 강제 업데이트 배포가 끝난 뒤에만 true 로 켠다.
    // 기본값 false: 토큰이 있으면 사용자 식별, 없으면 익명 연결 허용(기존 클라이언트 호환).
    @Value("${chat.websocket.auth-required:false}")
    private boolean webSocketAuthRequired;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/stomp");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry register) {
        register.addEndpoint(stompConnectUrl)
            .setAllowedOrigins("*");
            // .withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(64 * 1024)    // 64KB
                   .setSendBufferSizeLimit(512 * 1024) // 512KB
                   .setSendTimeLimit(20000);           // 20 seconds
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
        
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(new ObjectMapper());
        converter.setContentTypeResolver(resolver);
        
        messageConverters.add(converter);
        return false;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
                    return message;
                }

                // 채팅 메시지에 user_id 를 저장하려면 CONNECT 시점에 사용자를 식별해야 한다.
                // (식별 실패 시 메시지가 익명으로 저장되어 프로필 색상이 전부 null 이 된다.)
                String authorization = accessor.getFirstNativeHeader("Authorization");

                if (authorization == null || !authorization.startsWith("Bearer ")) {
                    if (webSocketAuthRequired) {
                        log.warn("WebSocket CONNECT 거부 - Authorization 헤더 없음 또는 형식 오류");
                        throw new MessageDeliveryException("WebSocket 연결에는 인증 토큰이 필요합니다.");
                    }
                    // 비강제 모드: 기존 클라이언트 호환을 위해 익명 연결 허용
                    log.warn("WebSocket 익명 연결 - Authorization 헤더 없음 (메시지가 user_id 없이 저장됨)");
                    return message;
                }

                String token = authorization.substring(7);
                try {
                    jwtUtil.validate(token);
                    Long userId = jwtUtil.getUserId(token);

                    accessor.setUser(new Principal() {
                        @Override
                        public String getName() {
                            return userId.toString();
                        }
                    });
                    log.info("WebSocket 인증 성공 - 사용자 ID: {}", userId);
                } catch (Exception e) {
                    log.error("WebSocket CONNECT - 토큰 검증 실패: {}", e.getMessage());
                    if (webSocketAuthRequired) {
                        throw new MessageDeliveryException("유효하지 않은 인증 토큰입니다.");
                    }
                    // 비강제 모드: 토큰이 유효하지 않아도 익명으로 연결 허용
                }

                return message;
            }
        });
    }
}