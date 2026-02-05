package com.github.motoryang.system.modules.notice.model.dto;

/**
 * 通知查询 DTO
 */
public record NoticeQueryDTO(
        String title,
        String type,
        String status,
        String priority,
        Integer current,
        Integer size
) {
    public NoticeQueryDTO {
        if (current == null || current < 1) {
            current = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
    }
}
