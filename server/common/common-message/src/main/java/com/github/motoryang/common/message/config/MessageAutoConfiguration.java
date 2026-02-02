package com.github.motoryang.common.message.config;

import com.github.motoryang.common.message.core.WsHandshakeHandler;
import com.github.motoryang.common.message.interceptor.WsHandshakeInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * 消息组件（WebSocket + RabbitMQ）自动配置类
 * 核心作用：整合 WebSocket (STOMP 协议) 和 RabbitMQ 相关配置，实现消息组件的自动装配、扫描和初始化
 * 支持通过配置文件动态修改 WebSocket 端点等参数，无需硬编码，提升组件的灵活性和可配置性
 */
@Configuration
@EnableWebSocketMessageBroker // 启用 WebSocket 消息代理，支持 STOMP 协议，实现点对点/广播消息传递
@EnableConfigurationProperties(MessageProperties.class)
@ComponentScan("com.github.motoryang.common.message") // 扫描指定包下的所有 Spring 组件（如 Helper 工具类、Listener 监听器），确保组件注入容器
@Import(RabbitConfig.class)
@AllArgsConstructor
public class MessageAutoConfiguration implements WebSocketMessageBrokerConfigurer {

    // 注入消息组件配置属性类，获取配置文件中配置的参数（如 WebSocket 端点地址）
    private final MessageProperties messageProperties;
    private final WsHandshakeInterceptor wsHandshakeInterceptor;
    private final WsHandshakeHandler wsHandshakeHandler;

    /**
     * 配置 WebSocket STOMP 端点（客户端连接 WebSocket 的入口地址）
     * 特点：端点地址从配置属性中动态获取，无需硬编码，支持配置文件灵活修改
     * @param registry STOMP 端点注册器，用于注册和配置 WebSocket 连接端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 从配置属性中获取 WebSocket 端点地址
        registry.addEndpoint(messageProperties.getEndpoint())
                // 允许所有来源的跨域请求（生产环境建议指定具体可信域名，提升安全性）
                .setAllowedOriginPatterns("*")
                // 拦截器提取header
                .addInterceptors(wsHandshakeInterceptor)
                // 自定义握手处理器绑定用户
                .setHandshakeHandler(wsHandshakeHandler)
                // 启用 SockJS 降级支持：当客户端不支持 WebSocket 时，自动切换为长轮询等兼容方案，提升兼容性
                .withSockJS();
    }

    /**
     * 配置 WebSocket 消息代理（消息路由规则、前缀配置）
     * 定义客户端与服务端之间的消息转发规范，区分广播、点对点、应用消息的前缀
     * @param registry 消息代理配置注册器，用于配置消息代理相关参数
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用内存消息代理（非分布式场景），支持 "/topic"（全局广播）和 "/queue"（点对点队列）前缀的消息转发
        // 分布式场景下：需替换为 RabbitMQ/Redis 分布式消息代理，实现跨服务节点消息同步
        registry.enableSimpleBroker("/topic", "/queue");

        // 配置应用消息前缀：客户端发送给服务端的业务消息，必须以 "/app" 为前缀，才能被 @MessageMapping 注解的方法接收
        // 示例：客户端发送消息到 "/app/chat/send"，对应服务端 @MessageMapping("/chat/send") 方法处理
        registry.setApplicationDestinationPrefixes("/app");

        // 配置用户点对点消息前缀：默认 "/user/"，框架会自动拼接用户 ID，实现专属消息推送
        // 工作机制：服务端发送 "/user/{userId}/xxx"，框架自动路由到该用户的专属队列，与 SimpMessagingTemplate.convertAndSendToUser 配合使用
        registry.setUserDestinationPrefix("/user/");
    }

}