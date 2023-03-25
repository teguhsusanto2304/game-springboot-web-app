package com.teguh.tictactoegame.config;

import com.teguh.tictactoegame.model.ActionMessage;
import com.teguh.tictactoegame.model.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

public class WebSocketEventListener {
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username != null) {
            ActionMessage chatMessage = new ActionMessage();
            chatMessage.setMessageType(MessageType.LEAVE);
            chatMessage.setName(username);
            messagingTemplate.convertAndSend("/topic/move", chatMessage);
        }
    }
}
