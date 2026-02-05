package com.github.motoryang.system.modules.notice.model.vo;

/**
 * 通知目标 VO
 */
public record NoticeTargetVO(
        String id,
        String noticeType,
        String targetId,
        String targetName
) {
}
