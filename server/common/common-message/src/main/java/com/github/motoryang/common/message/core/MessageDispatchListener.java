package com.github.motoryang.common.message.core;

import com.github.motoryang.common.message.constants.WsConstants;
import com.github.motoryang.common.message.dto.WsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * WebSocket 分布式消息分发监听器
 * 核心作用：监听 RabbitMQ 分发交换机的消息，根据不同路由键（全局/用户/角色），将消息转发给对应在线用户的 WebSocket 连接
 * 解决分布式部署下，跨服务节点的 WebSocket 消息同步问题（通过 RabbitMQ 统一分发，当前服务节点处理本地在线用户的消息推送）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageDispatchListener {

    // Spring WebSocket 消息模板，用于将 RabbitMQ 接收的消息，转发给本地在线用户的 WebSocket 连接
    private final SimpMessagingTemplate messagingTemplate;

    // 本地用户 Session 管理器，用于查询当前服务节点上的在线用户、用户角色关联等信息
    // TODO: 该方式在用户量庞大的时候，存在严重的性能问题，需要改进，暂置（建议后续替换为分布式 Session 存储，如 Redis）
    private final UserSessionManager sessionManager;

    /**
     * 监听 RabbitMQ 分布式消息分发交换机，处理三类消息（全局广播、指定用户、指定角色）
     * 采用动态临时队列（UUID 命名、自动删除），避免队列堆积，提高资源利用率
     * @param payload  RabbitMQ 接收的消息体，已通过 Jackson 序列化转换为 WsResponse 封装对象
     * @param routingKey  消息的路由键（从 AMQP 消息头中获取），用于区分消息类型（全局/用户/角色）
     */
    @RabbitListener(bindings = @QueueBinding(
            // 定义动态临时队列：每次启动生成唯一 UUID 作为队列名，服务停止/消费者断开后自动删除队列
            value = @Queue(name = "#{T(java.util.UUID).randomUUID().toString()}", autoDelete = "true"),
            // 绑定目标交换机：使用 WsConstants 中定义的分发交换机名称，交换机类型为 TOPIC（支持通配符路由）
            exchange = @Exchange(name = WsConstants.EXCHANGE_DISPATCH, type = ExchangeTypes.TOPIC),
            // 绑定路由键：支持三类路由键（用户通配符、全局、角色通配符），# 表示匹配任意后缀
            key = {WsConstants.KEY_USER + "#", WsConstants.KEY_ALL, WsConstants.KEY_ROLE + "#"}
    ))
    public void onMeesage(WsResponse<Object> payload, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        // 1. 处理全局广播消息：路由键匹配 "全部"，向所有在线用户推送 WebSocket 广播消息
        if (WsConstants.KEY_ALL.equals(routingKey)) {
            // 发送到 WebSocket 全局广播主题，所有订阅该主题的用户均可接收
            log.info("推送全局广播消息：{}", payload);
            messagingTemplate.convertAndSend(WsConstants.TOPIC_PUBLIC, payload);
        }

        // 2. 处理指定用户消息：路由键以用户前缀开头，向单个指定用户推送 WebSocket 点对点消息
        if (routingKey.startsWith(WsConstants.KEY_USER_PREFIX)) {
            // 截取路由键中的用户 ID（去除前缀，获取纯用户标识）
            String userId = routingKey.substring(WsConstants.KEY_USER_PREFIX.length());
            // 仅当用户连接在当前实例时发送（通过 WebSocket 点对点消息，推送给该用户的专属队列）
            messagingTemplate.convertAndSendToUser(userId, WsConstants.QUEUE_USER_REMIND, payload);
            return; // 处理完成，直接返回，避免后续逻辑执行
        }

        // 3. 处理指定角色消息：路由键以角色前缀开头，向该角色下所有在线用户推送 WebSocket 点对点消息
        if (routingKey.startsWith(WsConstants.KEY_ROLE_PREFIX)) {
            // 截取路由键中的角色编码（去除前缀，获取纯角色标识）
            String roleCode = routingKey.substring(WsConstants.KEY_ROLE_PREFIX.length());
            // 从本地 Session 管理器中找出所有具备该角色的在线用户（仅当前服务节点的在线用户）
            var users = sessionManager.getUsersByRole(roleCode);
            // 遍历用户列表，逐个推送点对点消息
            users.forEach(uid -> messagingTemplate.convertAndSendToUser(uid, WsConstants.QUEUE_USER_REMIND, payload));
        }
    }

}