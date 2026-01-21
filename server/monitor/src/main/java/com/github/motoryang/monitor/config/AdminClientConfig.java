package com.github.motoryang.monitor.config;

import de.codecentric.boot.admin.server.web.client.HttpHeadersProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class AdminClientConfig {

    @Bean
    public HttpHeadersProvider customHttpHeadersProvider(@Value("${auth.internal.secret}") String internalSecret) {
        return (instance) -> {
            var headers = new HttpHeaders();
            headers.add("X-Internal-Token", internalSecret);
            return headers;
        };
    }

}
