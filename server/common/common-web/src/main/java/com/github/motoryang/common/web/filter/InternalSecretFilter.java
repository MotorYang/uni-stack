package com.github.motoryang.common.web.filter;

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
 * Internal request filter: only allows requests with gateway-specific headers
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

        // Check if request is from gateway
        if (StringUtils.hasText(internalSecret) && internalSecret.equals(headerSecret)) {

            // Try to parse user info from gateway headers
            String userId = request.getHeader(Constants.USER_ID_HEADER);
            String username = request.getHeader(Constants.USERNAME_HEADER);
            if (StringUtils.hasText(userId) && StringUtils.hasText(username)) {
                // Build authentication object to pass internal service authorization
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, username, Collections.emptyList()
                );
                // Store authentication info in context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 403, \"msg\": \"Unauthorized access!\"}");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Skip this filter for documentation-related paths
        return path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/knife4j")
                || path.contains("/webjars");
    }
}
