package com.github.motoryang.auth.controller;

import com.github.motoryang.auth.model.dto.LoginDTO;
import com.github.motoryang.auth.model.dto.RefreshTokenDTO;
import com.github.motoryang.auth.model.vo.TokenVO;
import com.github.motoryang.auth.service.IAuthService;
import com.github.motoryang.common.core.constants.Constants;
import com.github.motoryang.common.core.result.RestResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 */
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public RestResult<TokenVO> login(@Valid @RequestBody LoginDTO dto) {
        TokenVO token = authService.login(dto);
        return RestResult.ok(token);
    }

    /**
     * 刷新 Token
     */
    @PostMapping("/refresh")
    public RestResult<TokenVO> refresh(@Valid @RequestBody RefreshTokenDTO dto) {
        TokenVO token = authService.refresh(dto);
        return RestResult.ok(token);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public RestResult<Void> logout(HttpServletRequest request) {
        String bearerToken = request.getHeader(Constants.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(Constants.TOKEN_PREFIX)) {
            String token = bearerToken.substring(Constants.TOKEN_PREFIX.length());
            authService.logout(token);
        }
        return RestResult.ok();
    }
}
