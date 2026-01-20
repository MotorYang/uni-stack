package com.github.motoryang.system.modules.permission.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 权限创建 DTO
 */
public record PermissionCreateDTO(
        @NotBlank(message = "权限名称不能为空")
        @Size(max = 50, message = "权限名称长度不能超过50个字符")
        String permName,

        @NotBlank(message = "权限编码不能为空")
        @Size(max = 100, message = "权限编码长度不能超过100个字符")
        String permCode,

        @Size(max = 200, message = "权限描述长度不能超过200个字符")
        String description,

        Integer sort
) {
}
