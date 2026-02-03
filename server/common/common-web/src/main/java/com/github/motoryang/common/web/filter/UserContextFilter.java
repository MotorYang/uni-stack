package com.github.motoryang.common.web.filter;

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
 * User context filter
 * <p>
 * Parses user info from Gateway-forwarded request headers and stores in UserContextHolder
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Get user info from request headers
            String userId = request.getHeader(Constants.USER_ID_HEADER);
            String username = request.getHeader(Constants.USERNAME_HEADER);
            String rolesStr = request.getHeader(Constants.USER_ROLES_HEADER);
            String permsStr = request.getHeader(Constants.USER_PERMS_HEADER);

            // Parse roles and permissions
            Set<String> roles = parseToSet(rolesStr);
            Set<String> permissions = parseToSet(permsStr);

            // Build LoginUser and store in context
            LoginUser loginUser = new LoginUser(userId, username, roles, permissions);
            UserContextHolder.setContext(loginUser);

            filterChain.doFilter(request, response);
        } finally {
            // Clear context after request ends to prevent memory leaks
            UserContextHolder.clearContext();
        }
    }

    /**
     * Parse comma-separated string to Set
     */
    private Set<String> parseToSet(String str) {
        if (!StringUtils.hasText(str)) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(str.split(",")));
    }
}
