package com.github.motoryang.system.config;

import com.github.motoryang.common.core.feign.FeignInternalRequestInterceptor;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Value("${auth.internal.secret}")
    private String internalSecret;

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return new FeignInternalRequestInterceptor(internalSecret);
    }

}
