package com.github.motoryang.system.modules.notice.model.vo;

import java.time.LocalDateTime;

/**
 * 通知 VO
 */
public record NoticeVO(
        String id,
        String title,
        String type,
        String priority,
        String status,
        String target,
        String timeOption,
        LocalDateTime noticeTime,
        String createBy,
        LocalDateTime createTime
) {
}
