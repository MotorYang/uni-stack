package com.github.motoryang.system.modules.notice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知创建 DTO
 */
public record NoticeCreateDTO(
        @NotBlank(message = "通知标题不能为空")
        @Size(max = 200, message = "通知标题长度不能超过200个字符")
        String title,

        @NotBlank(message = "通知内容不能为空")
        String content,

        @NotBlank(message = "通知类型不能为空")
        String type,

        String priority,

        @NotBlank(message = "通知目标不能为空")
        String target,

        String timeOption,

        LocalDateTime noticeTime,

        List<String> targetIds
) {
}
