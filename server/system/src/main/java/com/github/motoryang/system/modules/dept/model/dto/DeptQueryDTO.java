package com.github.motoryang.system.modules.dept.model.dto;

/**
 * 部门查询 DTO
 */
public record DeptQueryDTO(
        String deptName,
        Integer status
) {
}
