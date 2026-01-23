package com.github.motoryang.system.modules.dept.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 设置职务 DTO
 */
public record SetPositionDTO(
        @NotBlank(message = "用户ID不能为空")
        String userId,

        @NotBlank(message = "部门ID不能为空")
        String deptId,

        String position
) {
}
