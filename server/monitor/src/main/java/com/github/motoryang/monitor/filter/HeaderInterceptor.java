package com.github.motoryang.monitor.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 请求拦截器
 * <p>
 * 为每次监听请求新增一个内部服务请求密钥，避免被内部服务拦截
 */
@Component
public class HeaderInterceptor implements ClientHttpRequestInterceptor {

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    @Value("${auth.internal.secret}")
    private String internalSecret;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        var headers = request.getHeaders();
        if (!headers.containsKey(INTERNAL_TOKEN_HEADER)) {
            headers.add(INTERNAL_TOKEN_HEADER, internalSecret);
        }
        System.out.println("headers: " + headers);
        return execution.execute(request, body);
    }
}
