package com.github.motoryang.system.modules.resource.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 资源创建 DTO
 */
public record ResourceCreateDTO(
        @NotBlank(message = "资源组ID不能为空")
        String groupId,

        @NotBlank(message = "资源名称不能为空")
        @Size(max = 50, message = "资源名称长度不能超过50个字符")
        String resName,

        @NotBlank(message = "资源类型不能为空")
        @Pattern(regexp = "^(API|BUTTON)$", message = "资源类型必须是 API 或 BUTTON")
        String resType,

        @NotBlank(message = "资源路径不能为空")
        @Size(max = 255, message = "资源路径长度不能超过255个字符")
        String resPath,

        @NotBlank(message = "资源编码不能为空")
        @Size(max = 100, message = "资源编码长度不能超过100个字符")
        String resCode,

        @Size(max = 20, message = "请求方式长度不能超过20个字符")
        String resMethod,

        @Size(max = 200, message = "资源描述长度不能超过200个字符")
        String description,

        Integer sort
) {
}
