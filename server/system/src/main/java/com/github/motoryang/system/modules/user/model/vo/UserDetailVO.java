package com.github.motoryang.system.modules.user.model.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详情 VO
 */
public record UserDetailVO(
        String id,
        String username,
        String nickname,
        String email,
        String phone,
        String avatar,
        Integer gender,
        Integer status,
        String deptId,
        String deptName,
        List<String> roleIds,
        List<String> roles,
        List<String> permissions,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
