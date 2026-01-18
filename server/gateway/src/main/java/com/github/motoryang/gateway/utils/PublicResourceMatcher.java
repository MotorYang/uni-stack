package com.github.motoryang.gateway.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 公开资源匹配器
 * 使用 HashSet 实现 O(1) 的精确匹配和高效的前缀匹配
 */
@Component
public class PublicResourceMatcher {

    /**
     * 公开静态资源的路径前缀
     * 这些路径下的资源无需认证即可访问
     */
    private static final Set<String> PUBLIC_PATH_PREFIXES = Set.of(
            "/static/unistack/avatar/"
    );

    /**
     * 公开静态资源的路径关键词
     * 路径中包含这些关键词的资源无需认证
     */
    private static final Set<String> PUBLIC_PATH_KEYWORDS = Set.of(
            "/avatar/"
    );

    /**
     * 预计算的最小前缀长度，用于快速失败
     */
    private int minPrefixLength;

    @PostConstruct
    public void init() {
        minPrefixLength = PUBLIC_PATH_PREFIXES.stream()
                .mapToInt(String::length)
                .min()
                .orElse(0);
    }

    /**
     * 判断路径是否为公开静态资源（前缀匹配）
     * 用于 AuthGlobalFilter 跳过认证
     */
    public boolean isPublicByPrefix(String path) {
        if (path == null || path.length() < minPrefixLength) {
            return false;
        }
        for (String prefix : PUBLIC_PATH_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断路径是否包含公开资源关键词
     * 用于 StaticResourcesFilter 跳过 token 验证
     */
    public boolean isPublicByKeyword(String path) {
        if (path == null) {
            return false;
        }
        for (String keyword : PUBLIC_PATH_KEYWORDS) {
            if (path.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 综合判断是否为公开资源
     */
    public boolean isPublic(String path) {
        return isPublicByPrefix(path) || isPublicByKeyword(path);
    }
}
