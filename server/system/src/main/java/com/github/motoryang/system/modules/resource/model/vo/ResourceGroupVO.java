package com.github.motoryang.system.modules.resource.model.vo;

import java.time.LocalDateTime;

/**
 * 资源组 VO
 */
public record ResourceGroupVO(
        String id,
        String resGroupName,
        String resGroupCode,
        String description,
        String serviceName,
        Integer sort,
        Integer status,
        LocalDateTime createTime,
        Long resourceCount
) {
}
