package com.github.motoryang.system.modules.user.model.vo;

/**
 * 用户部门关联 VO
 */
public record UserDeptVO(
        String deptId,
        String deptName,
        String deptType,
        String companyId,
        String companyName,
        Integer isPrimary,
        String position
) {
}
