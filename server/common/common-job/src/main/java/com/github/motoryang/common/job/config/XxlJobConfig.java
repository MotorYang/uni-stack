package com.github.motoryang.common.job.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(XxlJobProperties.class)
public class XxlJobConfig {

    private static final Logger log = LoggerFactory.getLogger(XxlJobConfig.class);
    @Value("${server.port}")
    private int serverPort;
    @Value("${spring.application.name}")
    private String applicationName; // 自动获取当前微服务名称

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor(XxlJobProperties properties) {
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();

        var admin = properties.admin();
        var exe = properties.executor();

        if (!"true".equals(exe.enabled())) {
            return null;
        }
        executor.setEnabled(true);
        executor.setAdminAddresses(admin.addresses());
        executor.setAccessToken(properties.accessToken());
        if (StringUtils.hasText(exe.appname())) {
            executor.setAppname(exe.appname());
        } else {
            executor.setAppname(applicationName);
        }

        // 执行器端口是：服务端口 + 1000
        int exectorPort = properties.executor().port();
        if (exectorPort <= 0) {
            exectorPort = serverPort + 1000;
        }

        executor.setPort(exectorPort);
        log.info("加载XXL-JOB...");
        return executor;
    }

}
