package com.github.motoryang.system.modules.role.model.vo;

import java.time.LocalDateTime;

/**
 * 角色 VO
 */
public record RoleVO(
        String id,
        String roleName,
        String roleKey,
        Integer sort,
        Integer status,
        String remark,
        LocalDateTime createTime
) {
}
