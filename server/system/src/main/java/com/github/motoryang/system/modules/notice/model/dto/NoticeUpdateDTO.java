package com.github.motoryang.system.modules.notice.model.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知更新 DTO
 */
public record NoticeUpdateDTO(
        @Size(max = 200, message = "通知标题长度不能超过200个字符")
        String title,

        String content,

        String type,

        String priority,

        String target,

        String timeOption,

        LocalDateTime noticeTime,

        List<String> targetIds
) {
}
