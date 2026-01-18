package com.github.motoryang.gateway.filter;

import com.github.motoryang.gateway.utils.TokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 静态资源过滤器
 */
@Slf4j
@Component
public class StaticResourcesFilter implements GlobalFilter, Ordered {

    @Value("${auth.jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        if (path.startsWith("/static/")) {
            try {
                log.info("正在访问静态资源: {}", path);
                // 验证 Token
                String token = TokenUtils.getToken(request);
                if (!StringUtils.hasText(token)) {
                    return TokenUtils.unauthorized(exchange, "未提供认证令牌");
                }
                Claims claims = TokenUtils.parseToken(token, jwtSecret.getBytes(StandardCharsets.UTF_8));
                // 检查是否是 access token
                String tokenType = claims.get("tokenType", String.class);
                if (!"access".equals(tokenType)) {
                    return TokenUtils.unauthorized(exchange, "无效的令牌类型");
                }
            }  catch (ExpiredJwtException e) {
                return TokenUtils.unauthorized(exchange, "令牌已过期");
            } catch (Exception e) {
                log.warn("Token validation failed: {}", e.getMessage());
                return TokenUtils.unauthorized(exchange, "无效的认证令牌");
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
