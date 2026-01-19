package com.github.motoryang.common.core.feign;

import com.github.motoryang.common.core.constants.Constants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 统一拦截器：负责在服务间调用时透传 Header
 */
public class FeignInternalRequestInterceptor implements RequestInterceptor {

    private final String internalSecret;

    public FeignInternalRequestInterceptor(String internalSecret) {
        this.internalSecret = internalSecret;
    }

    @Override
    public void apply(RequestTemplate template) {
        // 1. 获取当前线程绑定的请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // 2. 透传网关密钥（必须传，否则被拦截器拦下）
            String secret = request.getHeader(Constants.INTERNAL_TOKEN_HEADER);
            if (secret == null) {
                secret = this.internalSecret; // 如果当前上下文拿不到，使用本地配置的保底密钥
            }
            template.header(Constants.INTERNAL_TOKEN_HEADER, secret);

            // 3. 透传用户认证 Token
            String authHeader = request.getHeader(Constants.AUTHORIZATION_HEADER);
            if (authHeader != null) {
                template.header(Constants.AUTHORIZATION_HEADER, authHeader);
            }
        } else {
            // 如果是异步任务调用 Feign，没有 RequestContext，直接塞入内部密钥即可
            template.header(Constants.INTERNAL_TOKEN_HEADER, internalSecret);
        }
    }
}