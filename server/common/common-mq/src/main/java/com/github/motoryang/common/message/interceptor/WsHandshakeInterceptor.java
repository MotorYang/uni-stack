package com.github.motoryang.common.message.interceptor;

import com.github.motoryang.common.message.constants.WsConstants;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * WebSocket 握手拦截器
 */
@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 从网关 Header 中获取用户信息
        var headers = request.getHeaders();
        String userId = headers.getFirst(WsConstants.USER_ID_HEADER);
        String username = headers.getFirst(WsConstants.USERNAME_HEADER);
        Set<String> roles = Optional.ofNullable(headers.getFirst(WsConstants.USER_ROLES_HEADER))
                .map(str -> Set.of(str.split(",")))
                .orElseGet(Set::of);

        if (userId != null) {
            // 存入 attributes，供下一步 HandshakeHandler 使用
            attributes.put(WsConstants.WS_HEADER_USER_ID, userId);
            attributes.put(WsConstants.WS_HEADER_USERNAME, username);
            attributes.put(WsConstants.WS_HEADER_USER_ROLES, roles);
            return true;
        }

        // 如果没有 UserId，说明网关漏截或非法访问，拒绝握手
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, @Nullable Exception exception) {

    }

}
