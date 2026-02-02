package com.github.motoryang.common.message.utils;

import com.github.motoryang.common.message.constants.WsConstants;
import com.github.motoryang.common.message.dto.WsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * 消息发送核心工具类
 */
@Component
@RequiredArgsConstructor
public class MessageHelper {

    // RabbitMQ 消息模板，用于发送 RabbitMQ 消息（普通/延迟）
    private final RabbitMessagingTemplate rabbitTemplate;

    /**
     * 点对点推送
     */
    public <T> void sendToUser(String userId, String module, String type, T data) {
        var resp = WsResponse.ok(module, type, data);
        rabbitTemplate.convertAndSend(WsConstants.EXCHANGE_DISPATCH, WsConstants.KEY_USER + userId, resp);
    }

    /**
     * 全站广播
     */
    public <T> void broadcast(String type, T data) {
        var resp = WsResponse.ok("system", type, data);
        rabbitTemplate.convertAndSend(WsConstants.EXCHANGE_DISPATCH, WsConstants.KEY_ALL, resp);
    }

    /**
     * 按角色推送
     * 逻辑：发往专门的角色 Exchange，由监听器判断
     */
    public <T> void sendToRole(String roleCode, String module, String type, T data) {
        var resp = WsResponse.ok(module, type, data);
        rabbitTemplate.convertAndSend(WsConstants.EXCHANGE_DISPATCH, WsConstants.KEY_ROLE + roleCode, resp);
    }

}