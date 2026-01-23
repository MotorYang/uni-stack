package com.github.motoryang.system.modules.dept.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 部门用户查询 DTO
 */
public record DeptUserQueryDTO(
        @NotBlank(message = "部门ID不能为空")
        String deptId,

        String username,

        String nickname,

        Integer status,

        Integer current,

        Integer size
) {
    public DeptUserQueryDTO {
        if (current == null || current < 1) {
            current = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
    }
}
