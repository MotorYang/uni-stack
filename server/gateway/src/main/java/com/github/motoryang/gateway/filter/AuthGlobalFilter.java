package com.github.motoryang.gateway.filter;

import com.github.motoryang.common.redis.constants.RedisConstants;
import com.github.motoryang.gateway.constants.Constants;
import com.github.motoryang.gateway.utils.PublicResourceMatcher;
import com.github.motoryang.gateway.utils.TokenUtils;
import com.github.motoryang.gateway.utils.WhiteListMatcher;
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
            var claims = TokenUtils.parseToken(token, secretBytes);
            var userId = claims.get("userId", String.class);
            var username = claims.get("username", String.class);

            // 一次性获取并校验，使用 Reactive 方式
            return reactiveStringRedisTemplate.opsForValue()
                    .get(RedisConstants.REDIS_TOKEN_KEY + userId)
                    .defaultIfEmpty("EMPTY") // 防止返回 Mono.empty() 导致后续逻辑不执行
                    .flatMap(redisToken -> {
                        if ("EMPTY".equals(redisToken)) {
                            return TokenUtils.unauthorized(exchange, "登录已过期");
                        }
                        if (!redisToken.equals(token)) {
                            return TokenUtils.unauthorized(exchange, "账号已在别处登录");
                        }
                        // 成功后构建 Request
                        var mutatedRequest = request.mutate()
                                .header(Constants.USER_ID_HEADER, userId)
                                .header(Constants.USERNAME_HEADER, username)
                                .build();
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
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
