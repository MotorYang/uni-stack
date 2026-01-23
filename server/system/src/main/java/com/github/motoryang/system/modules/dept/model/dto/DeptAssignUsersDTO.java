package com.github.motoryang.system.modules.dept.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 部门分配用户 DTO
 */
public record DeptAssignUsersDTO(
        @NotBlank(message = "部门ID不能为空")
        String deptId,

        @NotEmpty(message = "用户ID列表不能为空")
        List<String> userIds
) {
}
