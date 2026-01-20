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
import com.github.motoryang.common.redis.constants.RedisConstants;
import com.github.motoryang.common.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

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

        // 4. 生成token并存入Redis（Token 中携带角色信息）
        TokenVO tokenVO = generateTokens(user.id(), user.username(), user.roles());
        saveTokenToRedis(user.id(), tokenVO, user.permissions());

        return tokenVO;
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

        String username = JwtUtils.getUsername(refreshToken, jwtSecret);

        // 3. 重新获取用户信息（包含最新的角色和权限）
        RestResult<UserAuthInfo> result = systemUserClient.getUserByUsername(username);
        if (!result.isSuccess() || result.data() == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        UserAuthInfo user = result.data();

        // 4. 生成新的Token并存入redis
        TokenVO tokenVO = generateTokens(user.id(), user.username(), user.roles());
        saveTokenToRedis(user.id(), tokenVO, user.permissions());

        return tokenVO;
    }

    @Override
    public void logout(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            return;
        }

        // 将token、权限缓存从Redis移除
        try {
            String userId = JwtUtils.getUserId(accessToken, jwtSecret);
            redisTemplate.delete(RedisConstants.REDIS_TOKEN_KEY + userId);
            redisTemplate.delete(RedisConstants.REDIS_REFRESH_TOKEN_KEY + userId);
            redisTemplate.delete(RedisConstants.REDIS_USER_PERMS_KEY + userId);
        } catch (Exception e) {
            log.warn("Token 已过期或无效: {}", e.getMessage());
        }
    }

    private TokenVO generateTokens(String userId, String username, List<String> roles) {
        String accessToken = JwtUtils.createAccessToken(userId, username, roles, jwtSecret, accessTokenExpire);
        String refreshToken = JwtUtils.createRefreshToken(userId, username, jwtSecret, refreshTokenExpire);

        return new TokenVO(accessToken, refreshToken, accessTokenExpire / 1000);
    }

    private void saveTokenToRedis(String userId, TokenVO tokenVO, List<String> permissions) {
        var tokenKey = RedisConstants.REDIS_TOKEN_KEY + userId;
        var refreshKey = RedisConstants.REDIS_REFRESH_TOKEN_KEY + userId;
        var permsKey = RedisConstants.REDIS_USER_PERMS_KEY + userId;

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            var stringConn = (StringRedisConnection) connection;
            stringConn.setEx(tokenKey, (int) (accessTokenExpire / 1000), tokenVO.accessToken());
            stringConn.setEx(refreshKey, (int) (refreshTokenExpire / 1000), tokenVO.refreshToken());

            // 存储权限列表到 Redis Set
            if (!CollectionUtils.isEmpty(permissions)) {
                stringConn.del(permsKey);
                stringConn.sAdd(permsKey, permissions.toArray(new String[0]));
                stringConn.expire(permsKey, (int) (accessTokenExpire / 1000));
            }
            return null;
        });
    }
}
