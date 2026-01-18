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
     * 自定义请求头
     */
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USERNAME_HEADER = "X-Username";

    /**
     * Authorization Header
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

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

