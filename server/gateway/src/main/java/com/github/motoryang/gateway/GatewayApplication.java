package com.github.motoryang.gateway;

import com.github.motoryang.gateway.properties.WhitelistedProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Gateway 网关服务启动类
 */
@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan(
        basePackages = "com.github.motoryang.gateway.properties"
)
@EnableDiscoveryClient
public class GatewayApplication implements CommandLineRunner {

    @Resource
    private WhitelistedProperties whitelistedProperties;

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("====== 网关白名单 =====");
        for (String s : whitelistedProperties.whitelist()) {
            log.info(s);
        }
        log.info("====== 网关白名单 END ======");
    }
}
