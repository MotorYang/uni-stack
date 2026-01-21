package com.github.motoryang.system.modules.resource.model.dto;

/**
 * 资源组查询 DTO
 */
public record ResourceGroupQueryDTO(
        String resGroupName,
        String resGroupCode,
        String serviceName,
        Integer status
) {
}
