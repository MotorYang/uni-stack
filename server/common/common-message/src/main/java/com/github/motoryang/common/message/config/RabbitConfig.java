package com.github.motoryang.common.message.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ConnectionFactory.class)
public class RabbitConfig {

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        // 强制使用 JSON 序列化，不再使用 Java 原生字节码
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @ConditionalOnMissingBean(name = "rabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            // ConnectionFactory 只有当真正的服务启动时才会创建，所以这里忽略报错
            ConnectionFactory connectionFactory) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        // JDK 21 虚拟线程支持
        factory.setTaskExecutor(Executors.newVirtualThreadPerTaskExecutor());
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        return factory;
    }

}
