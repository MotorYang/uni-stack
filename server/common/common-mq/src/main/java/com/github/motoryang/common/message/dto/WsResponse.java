package com.github.motoryang.common.message.dto;

import java.io.Serializable;

/**
 * 统一 WebSocket 消息格式
 * @param module  消息所属模块
 * @param type    消息类型，例如：ERROR, SYSTEM_NOTICE, CHAT, FORCE_LOGOUT
 * @param code    业务状态码
 * @param data    业务数据
 * @param timestamp 时间戳
 */
public record WsResponse<T> (
        String module,
        String type,
        int code,
        T data,
        long timestamp
) implements Serializable {
    public static <T> WsResponse<T> ok(String module ,String type, T data) {
        return  new WsResponse<T>(module, type, 200, data, System.currentTimeMillis());
    }
}
