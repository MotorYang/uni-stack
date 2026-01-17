package com.github.motoryang.system.api;

import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.system.modules.user.entity.User;
import com.github.motoryang.system.modules.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户内部 API 控制器（供其他服务调用）
 */
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class UserApiController {

    private final IUserService userService;

    /**
     * 根据用户名获取用户信息（用于登录认证）
     */
    @GetMapping("/user/username/{username}")
    public RestResult<UserAuthInfo> getUserByUsername(@PathVariable String username) {
        User user = userService.getByUsername(username);
        if (user == null) {
            return RestResult.fail(1001, "用户不存在");
        }

        UserAuthInfo authInfo = new UserAuthInfo(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getNickname(),
                user.getStatus()
        );

        return RestResult.ok(authInfo);
    }

    public record UserAuthInfo(
            String id,
            String username,
            String password,
            String nickname,
            Integer status
    ) {
    }
}
