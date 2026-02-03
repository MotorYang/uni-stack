package com.github.motoryang.common.web.feign;

import com.github.motoryang.common.core.constants.Constants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign interceptor for internal service calls: transparently passes headers between services
 */
public class FeignInternalRequestInterceptor implements RequestInterceptor {

    private final String internalSecret;

    public FeignInternalRequestInterceptor(String internalSecret) {
        this.internalSecret = internalSecret;
    }

    @Override
    public void apply(RequestTemplate template) {
        // 1. Get the request object bound to the current thread
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // 2. Pass through gateway secret (required, otherwise intercepted)
            String secret = request.getHeader(Constants.INTERNAL_TOKEN_HEADER);
            if (secret == null) {
                secret = this.internalSecret; // Fallback to local configured secret
            }
            template.header(Constants.INTERNAL_TOKEN_HEADER, secret);

            // 3. Pass through user authentication token
            String authHeader = request.getHeader(Constants.AUTHORIZATION_HEADER);
            if (authHeader != null) {
                template.header(Constants.AUTHORIZATION_HEADER, authHeader);
            }
        } else {
            // For async tasks calling Feign without RequestContext, just use internal secret
            template.header(Constants.INTERNAL_TOKEN_HEADER, internalSecret);
        }
    }
}
