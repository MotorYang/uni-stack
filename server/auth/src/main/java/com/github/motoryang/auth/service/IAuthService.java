package com.github.motoryang.auth.service;

import com.github.motoryang.auth.model.dto.LoginDTO;
import com.github.motoryang.auth.model.dto.RefreshTokenDTO;
import com.github.motoryang.auth.model.vo.TokenVO;

/**
 * 认证服务接口
 */
public interface IAuthService {

    /**
     * 用户登录
     */
    TokenVO login(LoginDTO dto);

    /**
     * 刷新 Token
     */
    TokenVO refresh(RefreshTokenDTO dto);

    /**
     * 用户登出
     */
    void logout(String accessToken);
}
