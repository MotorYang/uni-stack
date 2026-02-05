package com.github.motoryang.system.modules.notice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知接收者状态表
 */
@Data
@TableName("sys_notice_receive")
public class NoticeReceive implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String noticeId;

    private String userId;

    private String isRead;

    private LocalDateTime readTime;
}
