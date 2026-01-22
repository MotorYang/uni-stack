package com.github.motoryang.gateway.messaging.consumer;

import com.github.motoryang.gateway.handler.PermissionCacheLoader;
import com.github.motoryang.gateway.messaging.event.PermissionSyncType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 权限消息消费者
 */
@Configuration
public class PermissionMessageConsumer {

    private final PermissionCacheLoader cacheLoader;

    // 防止并发刷新
    private final AtomicBoolean refreshing = new AtomicBoolean(false);
    // 时间窗口防抖（500ms）
    private final AtomicLong lastRefreshTime = new AtomicLong(0);

    public PermissionMessageConsumer(PermissionCacheLoader cacheLoader) {
        this.cacheLoader = cacheLoader;
    }

    @Bean
    Consumer<Message<PermissionSyncType>> permSync() {
        return message -> {
            PermissionSyncType type = message.getPayload();
            if (type != PermissionSyncType.REFRESH) {
                return;
            }
            // CAS 防抖：同一时间只允许一次刷新
            if (!refreshing.compareAndSet(false, true)) {
                return;
            }
            // 时间窗口防抖
            if (System.currentTimeMillis() - lastRefreshTime.get() < 500) {
                return;
            }
            lastRefreshTime.set(System.currentTimeMillis());
            // 虚拟线程从Redis加载权限
            Thread.startVirtualThread(() -> {
                try {
                    cacheLoader.loadFromRedis();
                } finally {
                    refreshing.set(false);
                }
            });
        };
    }

}
