package com.github.motoryang.monitor;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 监控中心启动类
 * <p>
 * 基于 Spring Boot Admin Server 实现微服务监控，
 * 通过 Nacos 服务发现自动注册和监控各个微服务实例
 *
 * @author motoryang
 */
@EnableAdminServer
@EnableDiscoveryClient
@SpringBootApplication
public class MonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorApplication.class, args);
    }
}
