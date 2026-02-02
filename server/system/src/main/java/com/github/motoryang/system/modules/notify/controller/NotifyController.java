package com.github.motoryang.system.modules.notify.controller;

import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.common.message.core.UserSessionManager;
import com.github.motoryang.common.message.utils.MessageHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "通知管理", description = "通知的增删改查")
@RestController
@RequestMapping("/notify")
@RequiredArgsConstructor
public class NotifyController {

    private final MessageHelper messageHelper;
    private final UserSessionManager sessionManager;

    @Operation(summary = "测试通知", description = "测试通知")
    @PostMapping("/test")
    public void testNotify(@RequestBody String message) {
        log.info("testNotify: {}", message);
        messageHelper.broadcast("SYSTEM_NOTICE",message);
    }

    @Operation(summary = "获取在线用户数量", description = "获取在线用户数量")
    @GetMapping("/onlineUserSize")
    public RestResult<Integer> onlineUserSize() {
        int count = sessionManager.getOnlineUserCount();
        log.info("在线用户数量: {}", count);
        return RestResult.ok(count);
    }

}
