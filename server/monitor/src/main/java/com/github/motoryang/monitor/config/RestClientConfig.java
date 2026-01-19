package com.github.motoryang.monitor.config;

import com.github.motoryang.monitor.filter.HeaderInterceptor;
import jakarta.annotation.Resource;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    @Resource
    private HeaderInterceptor headerInterceptor;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        var restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(headerInterceptor);
        return restTemplate;
    }

}
