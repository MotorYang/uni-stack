package com.github.motoryang.common.redis.constants;

public class RedisConstants {

    private RedisConstants() {}

    /**
     * Redis前缀
     */
    public static final String REDIS_TOKEN_KEY = "uni:token:";
    public static final String REDIS_REFRESH_TOKEN_KEY = "uni:refresh:";
    public static final String REDIS_USER = "uni:user:";

    /**
     * 用户权限缓存 key 前缀，完整 key 为 uni:perms:{userId}
     */
    public static final String REDIS_USER_PERMS_KEY = "uni:perms:";
}
