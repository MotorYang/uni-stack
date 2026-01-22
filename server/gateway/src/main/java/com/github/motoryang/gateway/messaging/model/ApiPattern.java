package com.github.motoryang.gateway.messaging.model;

import java.util.Set;

/**
 * API 权限模型
 */
public record ApiPattern (
        String method,
        String path,
        Set<String> roles
) {
}
