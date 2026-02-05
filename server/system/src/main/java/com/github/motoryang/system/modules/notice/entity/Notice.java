package com.github.motoryang.system.modules.notice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.motoryang.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知模块
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
public class Notice extends BaseEntity {

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 通知类型：NOTICE(通知), ANNOUNCE(公告), TASK(待办)
     */
    private String type;

    /**
     * 通知优先级：L, M, H
     */
    private String priority;

    /**
     * 通知状态：DRAFT(草稿), PUBLISHED(已发布), REVOKE(已撤销)
     */
    private String status;

    /**
     * 通知目标：ALL(所有用户), ROLE(角色用户), USER(特定用户)
     */
    private String target;

    /**
     * 通知时间选项：NOW(即时通知), TIME(定时通知)
     */
    private String timeOption;

    /**
     * 通知时间
     */
    private LocalDateTime noticeTime;

}
