package com.github.motoryang.system.modules.dept.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 部门创建 DTO
 */
public record DeptCreateDTO(
        String parentId,

        @NotBlank(message = "部门名称不能为空")
        @Size(max = 50, message = "部门名称长度不能超过50个字符")
        String deptName,

        Integer sort,

        String leader,

        String phone,

        @Email(message = "邮箱格式不正确")
        String email,

        String deptType
) {
    public DeptCreateDTO {
        if (parentId == null || parentId.isBlank()) {
            parentId = "0";
        }
        if (sort == null) {
            sort = 0;
        }
        if (deptType == null || deptType.isBlank()) {
            deptType = "D";
        }
    }
}
