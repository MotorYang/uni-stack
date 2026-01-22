package com.github.motoryang.gateway.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 权限缓存加载器
 * <p>
 * 从 Redis 读取权限缓存并加载到本地内存
 * Redis 缓存格式:
 * Key: perm:api:roles (HASH)
 * Field: GET:/api/user/**
 * Value: ADMIN,USER
 *
 * @author motoryang
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionCacheLoader implements ApplicationRunner {

    private static final String CACHE_KEY = "perm:api:roles";

    private final StringRedisTemplate redisTemplate;
    private final PermissionCacheHandler cacheHandler;

    @Override
    public void run(ApplicationArguments args) {
        loadFromRedis();
    }

    /**
     * 从 Redis 加载权限缓存到本地内存
     */
    public void loadFromRedis() {
        log.info("从 Redis 加载权限缓存...");
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(CACHE_KEY);

            if (entries.isEmpty()) {
                log.warn("Redis 中没有权限缓存数据，请确保 system 服务已启动并初始化缓存");
                cacheHandler.refreshAll(Map.of());
                return;
            }

            Map<String, Set<String>> apiToRoles = new HashMap<>();
            entries.forEach((k, v) -> {
                String apiPattern = k.toString();
                String roleKeys = v.toString();
                // 将逗号分隔的角色字符串转为 Set
                Set<String> roles = Set.of(roleKeys.split(","));
                apiToRoles.put(apiPattern, roles);
            });

            cacheHandler.refreshAll(apiToRoles);
            log.info("权限缓存加载完成，共 {} 条 API 规则", apiToRoles.size());
        } catch (Exception e) {
            log.error("从 Redis 加载权限缓存失败", e);
        }
    }
}
