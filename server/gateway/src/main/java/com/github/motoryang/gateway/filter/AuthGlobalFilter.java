package com.github.motoryang.gateway.filter;

import com.github.motoryang.gateway.constants.Constants;
import com.github.motoryang.gateway.properties.WhitelistedProperties;
import com.github.motoryang.gateway.utils.TokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 全局认证过滤器
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${auth.jwt.secret}")
    private String jwtSecret;

    @Resource
    private WhitelistedProperties whitelistedProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单路径直接放行
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        // 获取 Token
        String token = TokenUtils.getToken(request);
        if (!StringUtils.hasText(token)) {
            return TokenUtils.unauthorized(exchange, "请先登录");
        }

        // 验证 Token
        try {
            Claims claims = TokenUtils.parseToken(token, jwtSecret.getBytes(StandardCharsets.UTF_8));

            // 检查是否是 access token
            String tokenType = claims.get("tokenType", String.class);
            if (!"access".equals(tokenType)) {
                return TokenUtils.unauthorized(exchange, "无效的令牌类型");
            }

            String userId = claims.get("userId", String.class);
            String username = claims.get("username", String.class);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(Constants.USER_ID_HEADER, userId)
                    .header(Constants.USERNAME_HEADER, username)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (ExpiredJwtException e) {
            return TokenUtils.unauthorized(exchange, "令牌已过期");
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return TokenUtils.unauthorized(exchange, "无效的认证令牌");
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isWhitelisted(String path) {
        List<String> patterns = Arrays.asList(whitelistedProperties.whitelist());
        return patterns.stream()
                .map(String::trim)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

}
