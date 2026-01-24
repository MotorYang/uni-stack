package com.github.motoryang.system.modules.dept.model.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门 VO
 */
public record DeptVO(
        String id,
        String parentId,
        String ancestors,
        String deptName,
        Integer sort,
        String leader,
        String phone,
        String email,
        Integer status,
        String deptType,
        LocalDateTime createTime,
        List<DeptVO> children
) {
}
