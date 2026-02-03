package com.github.motoryang.common.message.constants;

/**
 * 消息常量
 */
public class WsConstants {

    public static final String EXCHANGE_DISPATCH = "ex.msg.dispatch";
    public static final String KEY_ALL = "msg.all";
    public static final String KEY_USER = "msg.user.";
    public static final String KEY_USER_PREFIX = "msg.user.";
    public static final String KEY_ROLE = "msg.role.";
    public static final String KEY_ROLE_PREFIX = "msg.role.";

    public static final String TOPIC_PUBLIC = "/topic/public";
    public static final String QUEUE_USER_REMIND = "/queue/remind";

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USERNAME_HEADER = "X-Username";
    public static final String USER_ROLES_HEADER = "X-User-Roles";

    public static final String WS_HEADER_USER_ID = "ws_user_id";
    public static final String WS_HEADER_USERNAME = "ws_username";
    public static final String WS_HEADER_USER_ROLES= "ws_user_roles";

}
