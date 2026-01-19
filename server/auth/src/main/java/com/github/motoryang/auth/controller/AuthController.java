package com.github.motoryang.auth.controller;

import com.github.motoryang.auth.model.dto.LoginDTO;
import com.github.motoryang.auth.model.dto.RefreshTokenDTO;
import com.github.motoryang.auth.model.vo.TokenVO;
import com.github.motoryang.auth.service.IAuthService;
import com.github.motoryang.common.core.constants.Constants;
import com.github.motoryang.common.core.result.RestResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 *
 * @author motoryang
 */
@Tag(name = "认证管理", description = "用户登录、登出、刷新Token等认证相关接口")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    /**
     * 用户登录
     *
     * @param dto 登录请求参数
     * @return Token信息
     */
    @Operation(summary = "用户登录", description = "通过用户名和密码进行登录认证，返回访问令牌和刷新令牌")
    @PostMapping("/login")
    public RestResult<TokenVO> login(@Valid @RequestBody LoginDTO dto) {
        TokenVO token = authService.login(dto);
        return RestResult.ok(token);
    }

    /**
     * 刷新 Token
     *
     * @param dto 刷新Token请求参数
     * @return 新的Token信息
     */
    @Operation(summary = "刷新Token", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh")
    public RestResult<TokenVO> refresh(@Valid @RequestBody RefreshTokenDTO dto) {
        TokenVO token = authService.refresh(dto);
        return RestResult.ok(token);
    }

    /**
     * 用户登出
     *
     * @param request HTTP请求对象
     * @return 操作结果
     */
    @Operation(summary = "用户登出", description = "退出登录，使当前Token失效")
    @PostMapping("/logout")
    public RestResult<Void> logout(
            @Parameter(hidden = true) HttpServletRequest request) {
        String bearerToken = request.getHeader(Constants.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(Constants.TOKEN_PREFIX)) {
            String token = bearerToken.substring(Constants.TOKEN_PREFIX.length());
            authService.logout(token);
        }
        return RestResult.ok();
    }
}
