package com.github.motoryang.common.message.dto;

import java.security.Principal;
import java.util.Set;

/**
 * WebSocket 用户信息
 * @param userId    用户ID
 * @param username  用户账号
 * @param roles     用户角色
 */
public record WsUser(
        String userId,
        String username,
        Set<String> roles
) implements Principal {

    @Override
    public String getName() {
        return username;
    }
}
