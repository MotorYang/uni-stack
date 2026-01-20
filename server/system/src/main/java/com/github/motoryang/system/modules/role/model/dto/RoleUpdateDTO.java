package com.github.motoryang.system.modules.role.model.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 角色更新 DTO
 */
public record RoleUpdateDTO(
        @Size(max = 50, message = "角色名称长度不能超过50个字符")
        String roleName,

        @Size(max = 50, message = "角色权限字符串长度不能超过50个字符")
        String roleKey,

        Integer sort,

        Integer status,

        String remark,

        List<String> menuIds,

        List<String> permissionIds
) {
}
