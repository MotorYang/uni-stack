package com.github.motoryang.system.modules.notice.model.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知详情 VO
 */
public record NoticeDetailVO(
        String id,
        String title,
        String content,
        String type,
        String priority,
        String status,
        String target,
        String timeOption,
        LocalDateTime noticeTime,
        List<NoticeTargetVO> targets,
        String createBy,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
