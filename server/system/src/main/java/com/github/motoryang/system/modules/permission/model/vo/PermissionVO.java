package com.github.motoryang.system.modules.permission.model.vo;

import java.time.LocalDateTime;
import java.util.List;

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
        LocalDateTime createTime,
        List<String> resourceIds
) {
}
