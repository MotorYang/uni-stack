package com.github.motoryang.system.modules.resource.model.dto;

/**
 * 资源查询 DTO
 */
public record ResourceQueryDTO(
        String groupId,
        String resName,
        String resType,
        String resCode,
        Integer status,
        Integer current,
        Integer size
) {
    public ResourceQueryDTO {
        if (current == null || current < 1) {
            current = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
    }
}
