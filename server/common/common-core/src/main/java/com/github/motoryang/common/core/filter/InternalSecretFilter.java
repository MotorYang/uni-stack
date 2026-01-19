package com.github.motoryang.common.core.filter;

import com.github.motoryang.common.core.constants.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * 内部请求拦截器：只允许带有网关特有请求头的请求通过
 */
public class InternalSecretFilter extends OncePerRequestFilter {

    private final String internalSecret;

    public InternalSecretFilter(String internalSecret) {
        this.internalSecret = internalSecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String headerSecret = request.getHeader(Constants.INTERNAL_TOKEN_HEADER);

        // 先判断是否是网关发来的请求
        if (StringUtils.hasText(internalSecret) && internalSecret.equals(headerSecret)) {

            // 尝试解析网关传来的用户信息
            String userId = request.getHeader(Constants.USER_ID_HEADER);
            String username = request.getHeader(Constants.USERNAME_HEADER);
            if (StringUtils.hasText(userId) && StringUtils.hasText(username)) {
                // 构建认证对象，以通过内部服务的鉴权认证
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, username, Collections.emptyList()
                );
                // 存储认证信息到上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 403, \"msg\": \"非法访问!\"}");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        System.out.println("当前正在请求的路径:" + path);
        // 只要是访问文档相关的路径，都不执行这个过滤器的校验逻辑
        return path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/knife4j")
                || path.contains("/webjars");
    }
}
