package com.github.motoryang.system.modules.resource.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 资源组创建 DTO
 */
public record ResourceGroupCreateDTO(
        @NotBlank(message = "资源组名称不能为空")
        @Size(max = 50, message = "资源组名称长度不能超过50个字符")
        String resGroupName,

        @NotBlank(message = "资源组编码不能为空")
        @Size(max = 50, message = "资源组编码长度不能超过50个字符")
        String resGroupCode,

        @Size(max = 200, message = "资源组描述长度不能超过200个字符")
        String description,

        @Size(max = 50, message = "服务名称长度不能超过50个字符")
        String serviceName,

        Integer sort
) {
}
