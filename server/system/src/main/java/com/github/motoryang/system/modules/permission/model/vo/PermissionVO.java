package com.github.motoryang.system.modules.permission.model.vo;

import java.time.LocalDateTime;

/**
 * 权限 VO
 */
public record PermissionVO(
        String id,
        String permName,
        String permCode,
        String description,
        Integer sort,
        Integer status,
        LocalDateTime createTime
) {
}
