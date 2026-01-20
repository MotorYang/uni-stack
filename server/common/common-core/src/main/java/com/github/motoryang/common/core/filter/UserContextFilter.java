package com.github.motoryang.common.core.filter;

import com.github.motoryang.common.core.constants.Constants;
import com.github.motoryang.common.core.context.LoginUser;
import com.github.motoryang.common.core.context.UserContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户上下文过滤器
 * <p>
 * 从 Gateway 转发的请求头中解析用户信息，并存入 UserContextHolder
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求头中获取用户信息
            String userId = request.getHeader(Constants.USER_ID_HEADER);
            String username = request.getHeader(Constants.USERNAME_HEADER);
            String rolesStr = request.getHeader(Constants.USER_ROLES_HEADER);
            String permsStr = request.getHeader(Constants.USER_PERMS_HEADER);

            // 解析角色和权限
            Set<String> roles = parseToSet(rolesStr);
            Set<String> permissions = parseToSet(permsStr);

            // 构建 LoginUser 并存入上下文
            LoginUser loginUser = new LoginUser(userId, username, roles, permissions);
            UserContextHolder.setContext(loginUser);

            filterChain.doFilter(request, response);
        } finally {
            // 请求结束后清除上下文，防止内存泄漏
            UserContextHolder.clearContext();
        }
    }

    /**
     * 将逗号分隔的字符串解析为 Set
     */
    private Set<String> parseToSet(String str) {
        if (!StringUtils.hasText(str)) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(str.split(",")));
    }
}
