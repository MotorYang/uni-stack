package com.github.motoryang.system.modules.user.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 用户更新 DTO
 */
public record UserUpdateDTO(
        @Size(min = 2, max = 50, message = "用户名长度必须在2-50个字符之间")
        String username,

        String nickname,

        @Email(message = "邮箱格式不正确")
        String email,

        String phone,

        String avatar,

        Integer gender,

        Integer status,

        String deptId,

        List<String> roleIds
) {
}
