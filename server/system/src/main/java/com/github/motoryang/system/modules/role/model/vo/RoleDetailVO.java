package com.github.motoryang.system.modules.role.model.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色详情 VO
 */
public record RoleDetailVO(
        String id,
        String roleName,
        String roleKey,
        Integer sort,
        Integer status,
        String remark,
        List<String> menuIds,
        List<String> permissionIds,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
