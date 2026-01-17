package com.github.motoryang.common.core.constants;

/**
 * 通用常量
 */
public final class Constants {

    private Constants() {
    }

    /**
     * Token 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Authorization Header
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Redis Key 前缀
     */
    public static final String REDIS_TOKEN_KEY = "auth:token:";
    public static final String REDIS_REFRESH_TOKEN_KEY = "auth:refresh:";
    public static final String REDIS_USER_KEY = "user:info:";

    /**
     * 状态常量
     */
    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_DISABLED = 1;

    /**
     * 删除标志
     */
    public static final int NOT_DELETED = 0;
    public static final int DELETED = 1;
}

