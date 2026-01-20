package com.github.motoryang.system.modules.permission.model.dto;

/**
 * 权限查询 DTO
 */
public record PermissionQueryDTO(
        String permName,
        String permCode,
        Integer status
) {
}
