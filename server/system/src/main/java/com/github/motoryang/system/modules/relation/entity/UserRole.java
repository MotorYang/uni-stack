package com.github.motoryang.system.modules.relation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户角色关联实体
 */
@Data
@TableName("sys_user_role")
public class UserRole {

    private String userId;

    private String roleId;
}
