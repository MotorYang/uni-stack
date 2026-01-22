package com.github.motoryang.system.message.producer;

import com.github.motoryang.system.message.model.PermissionSyncType;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * 权限事件生产者
 */
@Component
public class PermissionEventProducer {

    private final StreamBridge streamBridge;

    public PermissionEventProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishRefresh() {
        Message<PermissionSyncType> message = MessageBuilder.withPayload(PermissionSyncType.REFRESH).build();
        streamBridge.send("permSync-out-0", message);
    }

}
