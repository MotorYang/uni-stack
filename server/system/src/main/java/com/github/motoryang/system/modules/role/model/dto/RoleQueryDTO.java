package com.github.motoryang.system.modules.role.model.dto;

/**
 * 角色查询 DTO
 */
public record RoleQueryDTO(
        String roleName,
        String roleKey,
        Integer status,
        Integer current,
        Integer size
) {
    public RoleQueryDTO {
        if (current == null || current < 1) {
            current = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
    }
}
