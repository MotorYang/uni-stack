package com.github.motoryang.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * System 系统服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.github.motoryang.system", "com.github.motoryang.common"})
@EnableDiscoveryClient
@MapperScan("com.github.motoryang.system.modules.*.mapper")
public class SystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
    }
}
