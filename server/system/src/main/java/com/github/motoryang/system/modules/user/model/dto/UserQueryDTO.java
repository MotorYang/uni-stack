package com.github.motoryang.system.modules.user.model.dto;

/**
 * 用户查询 DTO
 */
public record UserQueryDTO(
        String username,
        String nickname,
        String phone,
        Integer status,
        String deptId,
        Integer current,
        Integer size
) {
    public UserQueryDTO {
        if (current == null || current < 1) {
            current = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
    }
}
