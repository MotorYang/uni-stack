package com.github.motoryang.auth.model.vo;

/**
 * Token 响应 VO
 */
public record TokenVO(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {
}
