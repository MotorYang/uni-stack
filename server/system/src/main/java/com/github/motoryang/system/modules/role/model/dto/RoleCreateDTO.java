package com.github.motoryang.system.modules.role.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 角色创建 DTO
 */
public record RoleCreateDTO(
        @NotBlank(message = "角色名称不能为空")
        @Size(max = 50, message = "角色名称长度不能超过50个字符")
        String roleName,

        @NotBlank(message = "角色权限字符串不能为空")
        @Size(max = 50, message = "角色权限字符串长度不能超过50个字符")
        String roleKey,

        Integer sort,

        String remark,

        List<String> menuIds
) {
}
