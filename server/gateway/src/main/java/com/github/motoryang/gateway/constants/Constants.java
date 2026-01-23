package com.github.motoryang.gateway.constants;

/**
 * 网关通用常量
 */
public class Constants {

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
    public static final String USER_ROLES_HEADER = "X-User-Roles";
    public static final String USER_PERMS_HEADER = "X-User-Perms";
    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    public static final String SKIP_SECURITY = "skipSecurity";
}
