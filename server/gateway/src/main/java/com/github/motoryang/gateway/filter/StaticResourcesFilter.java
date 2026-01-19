package com.github.motoryang.gateway.filter;

import com.github.motoryang.gateway.utils.PublicResourceMatcher;
import com.github.motoryang.gateway.utils.TokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 静态资源过滤器
 * 处理 /static/ 路径下的资源访问权限
 */
@Slf4j
@Component
public class StaticResourcesFilter implements GlobalFilter, Ordered {

    private static final String STATIC_PREFIX = "/static/";

    @Value("${auth.jwt.secret}")
    private String jwtSecret;

    private byte[] secretBytes;

    @Resource
    private PublicResourceMatcher publicResourceMatcher;

    @PostConstruct
    public void init() {
        this.secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        var path = request.getURI().getPath();

        // 只处理静态资源路径
        if (!path.startsWith(STATIC_PREFIX)) {
            return chain.filter(exchange);
        }

        // 公开资源跳过认证
        if (publicResourceMatcher.isPublicByKeyword(path)) {
            log.debug("访问公开静态资源: {}", path);
            return chain.filter(exchange);
        }

        // 私有静态资源需要验证 Token
        var token = TokenUtils.getToken(request);
        if (!StringUtils.hasText(token)) {
            return TokenUtils.unauthorized(exchange, "未提供认证令牌");
        }

        try {
            Claims claims = TokenUtils.parseToken(token, secretBytes);
            String tokenType = claims.get("tokenType", String.class);
            if (!"access".equals(tokenType)) {
                return TokenUtils.unauthorized(exchange, "无效的令牌类型");
            }
            return chain.filter(exchange);
        } catch (ExpiredJwtException e) {
            return TokenUtils.unauthorized(exchange, "令牌已过期");
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return TokenUtils.unauthorized(exchange, "无效的认证令牌");
        }
    }

    @Override
    public int getOrder() {
        return -90;
    }
}
