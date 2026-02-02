package com.github.motoryang.message;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Message 消息服务启动类
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.github.motoryang.message", "com.github.motoryang.common"})
//@MapperScan("com.github.motoryang.message.modules.*.mapper")
public class MessageApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessageApplication.class, args);
    }

}