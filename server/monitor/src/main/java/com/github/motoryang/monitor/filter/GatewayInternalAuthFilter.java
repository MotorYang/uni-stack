package com.github.motoryang.monitor.filter;

import com.github.motoryang.common.core.constants.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 网关内部鉴权过滤器
 * <p>
 * 验证来自网关的请求，如果携带正确的内部密钥，则自动完成认证，
 * 允许通过 Postman 等工具经由网关访问 monitor 的 API
 *
 * @author motoryang
 */
@Component
public class GatewayInternalAuthFilter extends OncePerRequestFilter {

    @Value("${auth.internal.secret}")
    private String internalSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String headerSecret = request.getHeader(Constants.INTERNAL_TOKEN_HEADER);

        // 如果携带了正确的网关内部密钥，自动完成认证
        if (StringUtils.hasText(internalSecret) && internalSecret.equals(headerSecret)) {
            // 创建一个具有管理员权限的认证对象
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    "gateway-internal",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
