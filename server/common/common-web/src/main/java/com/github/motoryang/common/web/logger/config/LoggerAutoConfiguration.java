package com.github.motoryang.common.web.logger.config;

import com.github.motoryang.common.web.logger.controller.LogFileController;
import com.github.motoryang.common.web.logger.service.LogFileService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto configuration for log file viewer.
 * Automatically enables log endpoints when spring.application.logger.enabled=true (default).
 */
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "spring.application.logger",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@ComponentScan(basePackageClasses = LogFileController.class)
public class LoggerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LogFileService logFileService() {
        return new LogFileService();
    }
}
