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

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = accessor.getFirstNativeHeader("Authorization");
                    log.info("WebSocket 연결 시도 - 토큰: {}", token != null ? "존재함" : "없음");

                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                        try {
                            // 토큰 검증
                            jwtUtil.validate(token);

                            // 사용자 ID 추출
                            Long userId = jwtUtil.getUserId(token);
                            log.info("WebSocket 인증 성공 - 사용자 ID: {}", userId);

                            accessor.setUser(new Principal() {
                                @Override
                                public String getName() {
                                    return userId.toString();
                                }
                            });
                        } catch (Exception e) {
                            // 토큰 검증 실패 시 처리
                            log.error("토큰 검증 실패: {}", e.getMessage());
                            throw new MessageDeliveryException("Invalid JWT token");
                        }
                    } else {
                        log.warn("Authorization 헤더가 없거나 Bearer 토큰이 아닙니다");
                    }
                }
                return message;
            }
        });
    }
}