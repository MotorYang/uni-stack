package com.github.motoryang.gateway.filter;

import com.github.motoryang.gateway.constants.Constants;
import com.github.motoryang.gateway.handler.PermissionCacheHandler;
import com.github.motoryang.gateway.utils.TokenUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class PermissionFilter implements GlobalFilter, Ordered {

    @Resource
    private PermissionCacheHandler permissionCacheHandler;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 白名单或公开静态资源跳过认证
        var skipSecurity = Boolean.TRUE.equals(exchange.getAttributes().get(Constants.SKIP_SECURITY));
        // TODO: 临时在后端修改下这里，让websocket先放过去，前端需要拆分资源，现在的资源设计不支持定义ws接口权限
        if (skipSecurity || exchange.getRequest().getURI().getPath().startsWith("/ws")) {
            return  chain.filter(exchange);
        }

        var request = exchange.getRequest();
        var path = request.getURI().getPath();
        var httpMethod = request.getMethod();
        List<String> roles = request.getHeaders().get(Constants.USER_ROLES_HEADER);

        if (roles == null || roles.isEmpty()) {
            return TokenUtils.unauthorized(exchange, "未获取到角色信息");
        }

        if (roles.contains("ADMIN")) {
            return chain.filter(exchange);
        }

        if (permissionCacheHandler.hasAccess(roles, httpMethod.name(), path)) {
            return chain.filter(exchange);
        }
        return TokenUtils.forbidden(exchange, "禁止访问该资源");
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
