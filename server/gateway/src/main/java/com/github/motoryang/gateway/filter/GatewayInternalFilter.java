package com.github.motoryang.gateway.filter;

import com.github.motoryang.gateway.constants.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 内部服务保护过滤器，给每个通过网关的请求添加特定的请求头，如果内部微服务没有校验到这个请求头，则拦截请求
 */
@Component
public class GatewayInternalFilter implements GlobalFilter, Ordered {

    @Value("${auth.internal.secret}")
    private String internalSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(Constants.INTERNAL_TOKEN_HEADER, internalSecret)
                .build();

        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
