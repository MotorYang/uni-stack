package com.github.motoryang.system.modules.resource.model.vo;

import java.time.LocalDateTime;

/**
 * 资源 VO
 */
public record ResourceVO(
        String id,
        String groupId,
        String resName,
        String resType,
        String resPath,
        String resCode,
        String resMethod,
        String description,
        Integer sort,
        Integer status,
        LocalDateTime createTime
) {
}
