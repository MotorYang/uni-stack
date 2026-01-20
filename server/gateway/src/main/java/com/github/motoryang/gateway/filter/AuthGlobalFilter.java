package com.github.motoryang.gateway.filter;

import com.github.motoryang.common.redis.constants.RedisConstants;
import com.github.motoryang.gateway.constants.Constants;
import com.github.motoryang.gateway.utils.PublicResourceMatcher;
import com.github.motoryang.gateway.utils.TokenUtils;
import com.github.motoryang.gateway.utils.WhiteListMatcher;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 全局认证过滤器
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Value("${auth.jwt.secret}")
    private String jwtSecret;

    private byte[] secretBytes;

    @Resource
    private WhiteListMatcher whiteListMatcher;
    @Resource
    private PublicResourceMatcher publicResourceMatcher;
    @Resource
    private ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    @PostConstruct
    public void init() {
        this.secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        var path = request.getURI().getPath();

        // 白名单或公开静态资源跳过认证
        if (whiteListMatcher.isWhitelisted(path) || publicResourceMatcher.isPublicByPrefix(path)) {
            return chain.filter(exchange);
        }

        var token = TokenUtils.getToken(request);
        if (!StringUtils.hasText(token)) {
            return TokenUtils.unauthorized(exchange, "请先登录");
        }

        try {
            // 解析 Claims
            Claims claims = TokenUtils.parseToken(token, secretBytes);
            String userId = claims.get("userId", String.class);
            String username = claims.get("username", String.class);
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            String rolesStr = roles != null ? String.join(",", roles) : "";

            // 校验 Redis 中的 Token，并获取权限列表
            return reactiveStringRedisTemplate.opsForValue()
                    .get(RedisConstants.REDIS_TOKEN_KEY + userId)
                    .defaultIfEmpty("EMPTY")
                    .flatMap(redisToken -> {
                        if ("EMPTY".equals(redisToken)) {
                            return TokenUtils.unauthorized(exchange, "登录已过期");
                        }
                        if (!redisToken.equals(token)) {
                            return TokenUtils.unauthorized(exchange, "账号已在别处登录");
                        }
                        // 从 Redis 获取权限列表
                        return reactiveStringRedisTemplate.opsForSet()
                                .members(RedisConstants.REDIS_USER_PERMS_KEY + userId)
                                .collectList()
                                .map(perms -> String.join(",", perms))
                                .defaultIfEmpty("")
                                .flatMap(permsStr -> {
                                    // 构建带有用户信息的 Request
                                    var mutatedRequest = request.mutate()
                                            .header(Constants.USER_ID_HEADER, userId)
                                            .header(Constants.USERNAME_HEADER, username)
                                            .header(Constants.USER_ROLES_HEADER, rolesStr)
                                            .header(Constants.USER_PERMS_HEADER, permsStr)
                                            .build();
                                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                                });
                    });

        } catch (ExpiredJwtException e) {
            return TokenUtils.unauthorized(exchange, "令牌已过期");
        } catch (Exception e) {
            return TokenUtils.unauthorized(exchange, "无效的认证令牌");
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

}
