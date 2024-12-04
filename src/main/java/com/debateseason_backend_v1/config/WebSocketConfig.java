package com.debateseason_backend_v1.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); //sub
        registry.setApplicationDestinationPrefixes("/stomp"); //pub
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry register) {
        register.addEndpoint("/ws-stomp")  .setAllowedOrigins("*");
        //.withSockJS();
    }



}