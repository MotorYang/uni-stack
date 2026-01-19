package com.github.motoryang.system.api;

import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.system.modules.user.entity.User;
import com.github.motoryang.system.modules.user.service.IUserService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户内部 API 控制器（供其他服务调用）
 *
 * @author motoryang
 */
@Hidden
@Tag(name = "用户内部API", description = "供其他微服务内部调用的用户接口，不对外暴露")
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class UserApiController {

    private final IUserService userService;

    /**
     * 根据用户名获取用户信息（用于登录认证）
     *
     * @param username 用户名
     * @return 用户认证信息
     */
    @Operation(summary = "根据用户名获取用户信息", description = "内部接口，用于登录认证时获取用户信息")
    @GetMapping("/user/username/{username}")
    public RestResult<UserAuthInfo> getUserByUsername(
            @Parameter(description = "用户名", required = true) @PathVariable String username) {
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

    /**
     * 用户认证信息
     *
     * @param id       用户ID
     * @param username 用户名
     * @param password 密码（加密后）
     * @param nickname 昵称
     * @param status   状态（0-禁用，1-正常）
     */
    @Schema(description = "用户认证信息")
    public record UserAuthInfo(
            @Schema(description = "用户ID")
            String id,
            @Schema(description = "用户名")
            String username,
            @Schema(description = "密码（加密后）")
            String password,
            @Schema(description = "昵称")
            String nickname,
            @Schema(description = "状态（0-禁用，1-正常）")
            Integer status
    ) {
    }
}
