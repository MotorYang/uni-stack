package com.github.motoryang.system.modules.dept.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 部门移除用户 DTO
 */
public record DeptRemoveUsersDTO(
        @NotBlank(message = "部门ID不能为空")
        String deptId,

        @NotEmpty(message = "用户ID列表不能为空")
        List<String> userIds
) {
}
