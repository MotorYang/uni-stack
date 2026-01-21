package com.github.motoryang.system.modules.resource.model.dto;

import jakarta.validation.constraints.Size;

/**
 * 资源组更新 DTO
 */
public record ResourceGroupUpdateDTO(
        @Size(max = 50, message = "资源组名称长度不能超过50个字符")
        String resGroupName,

        @Size(max = 200, message = "资源组描述长度不能超过200个字符")
        String description,

        @Size(max = 50, message = "服务名称长度不能超过50个字符")
        String serviceName,

        Integer sort,

        Integer status
) {
}
