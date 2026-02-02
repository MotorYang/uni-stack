package com.github.motoryang.common.message.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * 本地 Session 与角色缓存 (支持多端登录计数)
 */
@Slf4j
@Component
public class UserSessionManager {

    private final Map<String, Set<String>> userRolesMap = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> userConnectionCount = new ConcurrentHashMap<>();

    /**
     * 用户上线
     */
    public void addSession(String userId, String username, Set<String> roles) {
        userRolesMap.put(userId, roles);
        userConnectionCount.computeIfAbsent(userId, k -> new LongAdder()).increment();
        log.debug("用户上线: {} (ID: {}), 当前连接数: {}",
                username, userId, userConnectionCount.get(userId).sum());
    }

    /**
     * 用户下线
     */
    public void removeSession(String userId, String username) {
        userConnectionCount.computeIfPresent(userId, (id, counter) -> {
            counter.decrement();
            if (counter.sum() <= 0) {
                // 只有当所有端都断开时，才移除角色信息和计数器
                userRolesMap.remove(userId);
                log.debug("用户完全下线: {} (ID: {})", username, userId);
                return null; // 从 map 中移除计数器
            }
            log.debug("用户单端退出: {} (ID: {}), 剩余连接数: {}",
                    username, userId, counter.sum());
            return counter;
        });
    }

    /**
     * 获取当前去重后的在线人数
     */
    public int getOnlineUserCount() {
        return userRolesMap.size();
    }

    /**
     * 按角色筛选在线用户 ID 列表
     */
    public List<String> getUsersByRole(String roleCode) {
        return userRolesMap.entrySet().parallelStream()
                .filter(e -> e.getValue().contains(roleCode))
                .map(Map.Entry::getKey)
                .toList(); // Java 16+ 推荐使用 toList()
    }
}