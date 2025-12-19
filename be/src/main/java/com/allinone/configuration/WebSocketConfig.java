package com.allinone.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix cho các đường dẫn mà Client muốn NHẬN tin nhắn (Subscribe)
        // /topic: Chat nhóm (Public)
        // /user: Chat riêng (Private)
        registry.enableSimpleBroker("/topic", "/user");

        // Cấu hình cho chat riêng tư (1-1)
        registry.setUserDestinationPrefix("/user");
    }
}