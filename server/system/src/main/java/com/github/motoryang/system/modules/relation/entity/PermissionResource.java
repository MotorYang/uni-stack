package com.github.motoryang.system.modules.relation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 权限资源关联实体
 */
@Data
@TableName("sys_permission_resource")
public class PermissionResource {

    private String permId;

    private String resId;
}
