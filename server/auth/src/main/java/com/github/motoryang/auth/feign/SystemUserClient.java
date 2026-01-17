package com.github.motoryang.auth.feign;

import com.github.motoryang.auth.feign.fallback.SystemUserClientFallback;
import com.github.motoryang.common.core.result.RestResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * System 服务用户接口 Feign Client
 */
@FeignClient(name = "system", path = "/api/internal", fallbackFactory = SystemUserClientFallback.class)
public interface SystemUserClient {

    /**
     * 根据用户名获取用户信息（用于登录认证）
     */
    @GetMapping("/user/username/{username}")
    RestResult<UserAuthInfo> getUserByUsername(@PathVariable("username") String username);

    record UserAuthInfo(
            String id,
            String username,
            String password,
            String nickname,
            Integer status
    ) {
    }
}
