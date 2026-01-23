package com.github.motoryang.system.modules.relation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.motoryang.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户部门关联实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_dept")
public class UserDept extends BaseEntity {

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 部门ID
     */
    private String deptId;

    /**
     * 是否主职部门（0否 1是）
     */
    private Integer isPrimary;

    /**
     * 职务/岗位
     */
    private String position;
}
