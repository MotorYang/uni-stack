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
import com.github.motoryang.common.security.utils.JwtUtils;
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

import java.time.Duration;
import java.util.List;
import java.util.Objects;

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

    @Value("${auth.jwt.access-token-expire}")
    private Duration accessTokenExpire;

    @Value("${auth.jwt.refresh-token-expire}")
    private Duration refreshTokenExpire;

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

        // 3. 比对和Redis中的Token是否一致
        boolean isMatch = Objects.equals(
                redisTemplate.opsForValue()
                        .get(Constants.REDIS_REFRESH_TOKEN_KEY + JwtUtils.getUserId(refreshToken, jwtSecret)),
                dto.refreshToken());
        if (!isMatch) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        String username = JwtUtils.getUsername(refreshToken, jwtSecret);

        // 4. 重新获取用户信息（包含最新的角色和权限）
        RestResult<UserAuthInfo> result = systemUserClient.getUserByUsername(username);
        if (!result.isSuccess() || result.data() == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        UserAuthInfo user = result.data();

        // 5. 生成新的Token并存入redis
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
            redisTemplate.delete(Constants.REDIS_TOKEN_KEY + userId);
            redisTemplate.delete(Constants.REDIS_REFRESH_TOKEN_KEY + userId);
            redisTemplate.delete(Constants.REDIS_USER_PERMS_KEY + userId);
        } catch (Exception e) {
            log.warn("Token 已过期或无效: {}", e.getMessage());
        }
    }

    private TokenVO generateTokens(String userId, String username, List<String> roles) {
        String accessToken = JwtUtils.createAccessToken(userId, username, roles, jwtSecret, accessTokenExpire.toMillis());
        String refreshToken = JwtUtils.createRefreshToken(userId, username, jwtSecret, refreshTokenExpire.toMillis());

        return new TokenVO(accessToken, refreshToken, accessTokenExpire.toSeconds());
    }

    private void saveTokenToRedis(String userId, TokenVO tokenVO, List<String> permissions) {
        var tokenKey = Constants.REDIS_TOKEN_KEY + userId;
        var refreshKey = Constants.REDIS_REFRESH_TOKEN_KEY + userId;
        var permsKey = Constants.REDIS_USER_PERMS_KEY + userId;

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            var stringConn = (StringRedisConnection) connection;
            stringConn.setEx(tokenKey, (int) (accessTokenExpire.toSeconds()), tokenVO.accessToken());
            stringConn.setEx(refreshKey, (int) (refreshTokenExpire.toSeconds()), tokenVO.refreshToken());

            // 存储权限列表到 Redis Set
            if (!CollectionUtils.isEmpty(permissions)) {
                stringConn.del(permsKey);
                stringConn.sAdd(permsKey, permissions.toArray(new String[0]));
                stringConn.expire(permsKey, (int) (accessTokenExpire.toSeconds()));
            }
            return null;
        });
    }
}
