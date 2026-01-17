package com.github.motoryang.system.modules.relation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色菜单关联实体
 */
@Data
@TableName("sys_role_menu")
public class RoleMenu {

    private String roleId;

    private String menuId;
}
