package com.github.motoryang.system.handler;

import com.github.motoryang.system.modules.resource.mapper.ResourceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限缓存处理器
 * <p>
 * 将 API 资源与角色的映射关系缓存到 Redis
 * 缓存格式:
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

    private final ResourceMapper resourceMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        refreshCache();
    }

    /**
     * 刷新权限缓存
     */
    public void refreshCache() {
        log.info("开始刷新权限缓存到 Redis...");
        try {
            // 查询所有 API 资源与角色的映射关系
            List<Map<String, String>> mappings = resourceMapper.selectApiRoleMappings();

            // 构建缓存数据
            Map<String, String> cacheData = new HashMap<>();
            for (Map<String, String> mapping : mappings) {
                String apiPattern = mapping.get("apiPattern");
                String roleKeys = mapping.get("roleKeys");
                if (apiPattern != null && roleKeys != null) {
                    cacheData.put(apiPattern, roleKeys);
                }
            }

            // 清空旧缓存并写入新数据
            redisTemplate.delete(CACHE_KEY);
            if (!cacheData.isEmpty()) {
                redisTemplate.opsForHash().putAll(CACHE_KEY, cacheData);
            }

            log.info("权限缓存刷新完成，共 {} 条 API 规则", cacheData.size());
        } catch (Exception e) {
            log.error("刷新权限缓存失败", e);
        }
    }
}
