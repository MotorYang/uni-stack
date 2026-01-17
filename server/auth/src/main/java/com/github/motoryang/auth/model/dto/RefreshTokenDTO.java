package com.github.motoryang.auth.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新 Token 请求 DTO
 */
public record RefreshTokenDTO(
        @NotBlank(message = "刷新令牌不能为空")
        String refreshToken
) {
}
