package com.github.motoryang.system.modules.user.model.vo;

/**
 * 用户部门关联 VO
 */
public record UserDeptVO(
        String deptId,
        String deptName,
        Integer isPrimary,
        String position
) {
}
