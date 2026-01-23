package com.github.motoryang.system.modules.user.model.vo;

import java.time.LocalDateTime;

/**
 * 用户 VO
 */
public record UserVO(
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
        String position,
        LocalDateTime createTime
) {
}
