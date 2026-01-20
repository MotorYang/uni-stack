package com.github.motoryang.system.modules.permission.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.motoryang.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class Permission extends BaseEntity {

    /**
     * 权限名称
     */
    private String permName;

    /**
     * 权限编码（如 user:read, user:write, user:delete）
     */
    private String permCode;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 状态（0正常 1停用）
     */
    private Integer status;
}
