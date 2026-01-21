package com.github.motoryang.common.redis.api;

import java.io.Serializable;

/**
 * API 信息记录
 *
 * @param path        API 路径，如 "/users/{id}"
 * @param method      请求方法，如 "GET", "POST", "*"
 * @param summary     摘要，来自 @Operation 注解
 * @param description 描述，来自 @Operation 注解
 * @param tags        标签，来自 @Tag 注解
 */
public record ApiInfo(
        String path,
        String method,
        String summary,
        String description,
        String[] tags
) implements Serializable {
}
