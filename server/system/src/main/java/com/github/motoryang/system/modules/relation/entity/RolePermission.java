package com.github.motoryang.system.modules.relation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色权限关联实体
 */
@Data
@TableName("sys_role_permission")
public class RolePermission {

    private String roleId;

    private String permissionId;
}
