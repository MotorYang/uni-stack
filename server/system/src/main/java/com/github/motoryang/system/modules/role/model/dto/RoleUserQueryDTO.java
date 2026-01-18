package com.github.motoryang.system.modules.role.model.dto;

/**
 * 角色下用户查询DTO
 */
public record RoleUserQueryDTO(
        String roleId,
        String username,
        String nickname,
        Integer current,
        Integer size
) {
    public RoleUserQueryDTO {
        if (current == null || current < 1) {
            current = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
    }
}
