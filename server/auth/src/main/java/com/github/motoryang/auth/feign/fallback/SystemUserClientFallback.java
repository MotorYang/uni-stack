package com.github.motoryang.auth.feign.fallback;

import com.github.motoryang.auth.feign.SystemUserClient;
import com.github.motoryang.common.core.result.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * System 用户服务降级处理
 */
@Slf4j
@Component
public class SystemUserClientFallback implements FallbackFactory<SystemUserClient> {

    @Override
    public SystemUserClient create(Throwable cause) {
        log.error("System 用户服务调用失败: {}", cause.getMessage());
        return new SystemUserClient() {
            @Override
            public RestResult<UserAuthInfo> getUserByUsername(String username) {
                return RestResult.fail(503, "系统服务暂不可用");
            }
        };
    }
}
