package com.github.motoryang.system.modules.notice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通知目标实体
 */
@Data
@TableName("sys_notice_target")
public class NoticeTarget implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 通知用户类型：ROLE(角色), USER(用户)
     */
    private String noticeType;

    /**
     * 通知ID
     */
    private String noticeId;

    /**
     * 通知目标ID
     */
    private String targetId;

}
