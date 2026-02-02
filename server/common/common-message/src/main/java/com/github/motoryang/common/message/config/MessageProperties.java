package com.github.motoryang.common.message.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "uni.message")
public class MessageProperties {

    private String endpoint = "/ws";
    private String publicTopic = "/topic/public";
    private String userQueue = "/queue/remind";
    private String appPrefixes = "/app";

}
