package com.github.motoryang.common.message.core;

import com.github.motoryang.common.message.constants.WsConstants;
import com.github.motoryang.common.message.dto.WsUser;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

@Component
public class WsHandshakeHandler extends DefaultHandshakeHandler {

    @Nullable
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        var userId = (String) attributes.get(WsConstants.WS_HEADER_USER_ID);
        var userName = (String) attributes.get(WsConstants.WS_HEADER_USERNAME);
        var roles = (Set<String>) attributes.get(WsConstants.WS_HEADER_USER_ROLES);
        return new WsUser(userId, userName, roles);
    }
}
