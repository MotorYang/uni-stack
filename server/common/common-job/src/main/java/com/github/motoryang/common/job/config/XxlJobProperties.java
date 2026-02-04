package com.github.motoryang.common.job.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * XXL-JOB 配置属性映射
 */
@ConfigurationProperties(prefix = "xxl.job")
public record XxlJobProperties(
        Admin admin,
        Executor executor,
        @DefaultValue("")
        String accessToken
) {

    public record Admin(String addresses) {}

    public record Executor(
            @DefaultValue("false")
            String enabled,
            String appname,
            String address,
            String ip,
            int port,
            @DefaultValue("logs/xxl-job/jobhandler")
            String logpath,
            @DefaultValue("15")
            int logretentiondays
    ) {}
}
