package com.github.motoryang.gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.cloud.gateway")
public record WhitelistedProperties( String[] whitelist ) {
}
