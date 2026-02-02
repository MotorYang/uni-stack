package com.github.motoryang.common.message.core;

import com.github.motoryang.common.message.dto.WsUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@AllArgsConstructor
public class WsEventListener {

    private final UserSessionManager sessionManager;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        var accessor = StompHeaderAccessor.wrap(event.getMessage());
        var user = (WsUser) accessor.getUser();
        if (user != null) {
            sessionManager.addSession(user.userId(), user.username(), user.roles());
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        var accessor = StompHeaderAccessor.wrap(event.getMessage());
        var user = (WsUser) accessor.getUser();
        if (user != null) {
            sessionManager.removeSession(user.userId(), user.username());
        }
    }

}
