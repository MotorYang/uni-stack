package com.github.motoryang.system.modules.resource.model.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 资源更新 DTO
 */
public record ResourceUpdateDTO(
        @Size(max = 50, message = "资源名称长度不能超过50个字符")
        String resName,

        @Pattern(regexp = "^(API|BUTTON)$", message = "资源类型必须是 API 或 BUTTON")
        String resType,

        @Size(max = 255, message = "资源路径长度不能超过255个字符")
        String resPath,

        @Size(max = 20, message = "请求方式长度不能超过20个字符")
        String resMethod,

        @Size(max = 200, message = "资源描述长度不能超过200个字符")
        String description,

        Integer sort,

        Integer status
) {
}
