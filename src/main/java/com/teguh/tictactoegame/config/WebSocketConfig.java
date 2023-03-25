package com.teguh.tictactoegame.config;

import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }


    @Override
    public void registerStompEndpoints( StompEndpointRegistry registry )
    {
        registry.addEndpoint( "/websocket" )
                .setAllowedOrigins( "*" )
                .withSockJS( )
                .setClientLibraryUrl( "https://cdn.jsdelivr.net/sockjs/1.1.4/sockjs.min.js" );
    }
}
