package com.github.motoryang.system.modules.user.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 用户创建 DTO
 */
public record UserCreateDTO(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 2, max = 50, message = "用户名长度必须在2-50个字符之间")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
        String password,

        String nickname,

        @Email(message = "邮箱格式不正确")
        String email,

        String phone,

        Integer gender,

        String deptId,

        List<String> roleIds
) {
}
