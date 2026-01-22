package com.github.motoryang.gateway.handler;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.motoryang.gateway.messaging.model.ApiPattern;
import com.github.motoryang.gateway.utils.WhiteListMatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Gateway 权限缓存处理器
 * <p>
 * 只负责：
 *  API(resource) + Role → 是否放行
 */
@Slf4j
@Component
public class PermissionCacheHandler {

    private final WhiteListMatcher whiteListMatcher;

    public PermissionCacheHandler(WhiteListMatcher whiteListMatcher) {
        this.whiteListMatcher = whiteListMatcher;
    }

    // 预编译后的API权限列表
    private volatile List<ApiPattern> apiPatterns = List.of();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 热点缓存：
     * requestKey(GET:/api/user/1) -> allowedRoles
     */
    private final Cache<String, Set<String>> matchResultCache =
            Caffeine.newBuilder()
                    .maximumSize(2000)
                    .expireAfterWrite(2, TimeUnit.HOURS)
                    .build();

    /**
     * 鉴权入口
     */
    public boolean hasAccess(Collection<String> userRoles, String method, String path) {
        // 白名单判断
        if (whiteListMatcher.isWhitelisted(path)) {
            return true;
        }
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }
        String requestKey = method + ":" + path;
        Set<String> allowedRoles = matchResultCache.get(
                requestKey,
                key -> matchRoles(method, path)
        );
        if (allowedRoles == null || allowedRoles.isEmpty()) {
            return false;
        }
        // role 交集判定
        return userRoles.stream().anyMatch(allowedRoles::contains);
    }

    /**
     * API 模式匹配，找到允许访问的角色集合
     */
    private Set<String> matchRoles(String method, String path) {
        return apiPatterns.stream()
                .filter(entry ->
                        entry.method().equalsIgnoreCase(method)
                        && pathMatcher.match(entry.path(), path)
                )
                .flatMap(entry -> entry.roles().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    private boolean match(String apiPattern, String method, String path) {
        String[] parts = apiPattern.split(":", 2);
        return parts.length == 2
                && method.equalsIgnoreCase(parts[0])
                && pathMatcher.match(parts[1], path);
    }

    /**
     * 从 Redis 全量刷新（由 MQ 触发）
     */
    public void refreshAll(Map<String, Set<String>> apiToRoles) {

        List<ApiPattern> compiled = apiToRoles.entrySet().stream()
                .map(e -> {
                    String[] parts = e.getKey().split(":", 2);
                    return new ApiPattern(parts[0].toUpperCase(), parts[1], Set.copyOf(e.getValue()));
                }).toList();

        this.apiPatterns = compiled;
        matchResultCache.invalidateAll();

        log.info("Gateway permission cache refreshed, apiSize={}", compiled.size());
    }
}
