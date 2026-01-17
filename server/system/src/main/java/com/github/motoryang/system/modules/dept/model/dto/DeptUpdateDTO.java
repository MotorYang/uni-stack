package com.github.motoryang.system.modules.dept.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * 部门更新 DTO
 */
public record DeptUpdateDTO(
        String parentId,

        @Size(max = 50, message = "部门名称长度不能超过50个字符")
        String deptName,

        Integer sort,

        String leader,

        String phone,

        @Email(message = "邮箱格式不正确")
        String email,

        Integer status
) {
}
