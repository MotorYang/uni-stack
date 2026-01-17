package com.github.motoryang.auth.service.impl;

import com.github.motoryang.auth.feign.SystemUserClient;
import com.github.motoryang.auth.feign.SystemUserClient.UserAuthInfo;
import com.github.motoryang.auth.model.dto.LoginDTO;
import com.github.motoryang.auth.model.dto.RefreshTokenDTO;
import com.github.motoryang.auth.model.vo.TokenVO;
import com.github.motoryang.auth.service.IAuthService;
import com.github.motoryang.common.core.constants.Constants;
import com.github.motoryang.common.core.exception.BusinessException;
import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.common.core.result.ResultCode;
import com.github.motoryang.common.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final SystemUserClient systemUserClient;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @Value("${auth.jwt.secret}")
    private String jwtSecret;

    @Value("${auth.jwt.access-token-expire:7200000}")
    private long accessTokenExpire;  // 默认 2 小时

    @Value("${auth.jwt.refresh-token-expire:604800000}")
    private long refreshTokenExpire;  // 默认 7 天

    @Override
    public TokenVO login(LoginDTO dto) {
        // 1. 调用 system 服务获取用户信息
        RestResult<UserAuthInfo> result = systemUserClient.getUserByUsername(dto.username());
        if (!result.isSuccess() || result.data() == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        UserAuthInfo user = result.data();

        // 2. 校验用户状态
        if (user.status() != Constants.STATUS_NORMAL) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 3. 校验密码
        if (!passwordEncoder.matches(dto.password(), user.password())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 4. 生成双令牌
        return generateTokens(user.id(), user.username());
    }

    @Override
    public TokenVO refresh(RefreshTokenDTO dto) {
        String refreshToken = dto.refreshToken();

        // 1. 校验 refresh token
        if (!JwtUtils.validateToken(refreshToken, jwtSecret)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 2. 检查 token 类型
        String tokenType = JwtUtils.getTokenType(refreshToken, jwtSecret);
        if (!JwtUtils.TOKEN_TYPE_REFRESH.equals(tokenType)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 3. 检查是否在黑名单中
        String redisKey = Constants.REDIS_REFRESH_TOKEN_KEY + refreshToken;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            throw new BusinessException(ResultCode.TOKEN_REFRESH_EXPIRED);
        }

        String userId = JwtUtils.getUserId(refreshToken, jwtSecret);
        String username = JwtUtils.getUsername(refreshToken, jwtSecret);

        // 5. 将旧的 refresh token 加入黑名单
        long remainingTime = JwtUtils.getTokenRemainingTime(refreshToken, jwtSecret);
        if (remainingTime > 0) {
            redisTemplate.opsForValue().set(redisKey, "revoked", remainingTime, TimeUnit.MILLISECONDS);
        }

        return generateTokens(userId, username);
    }

    @Override
    public void logout(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            return;
        }

        // 将 access token 加入黑名单
        try {
            long remainingTime = JwtUtils.getTokenRemainingTime(accessToken, jwtSecret);
            if (remainingTime > 0) {
                String redisKey = Constants.REDIS_TOKEN_KEY + accessToken;
                redisTemplate.opsForValue().set(redisKey, "revoked", remainingTime, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            log.warn("Token 已过期或无效: {}", e.getMessage());
        }
    }

    private TokenVO generateTokens(String userId, String username) {
        String accessToken = JwtUtils.createAccessToken(userId, username, jwtSecret, accessTokenExpire);
        String refreshToken = JwtUtils.createRefreshToken(userId, username, jwtSecret, refreshTokenExpire);

        return new TokenVO(accessToken, refreshToken, accessTokenExpire / 1000);
    }
}
