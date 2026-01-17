package com.github.motoryang.system.modules.menu.model.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单 VO
 */
public record MenuVO(
        String id,
        String parentId,
        String menuName,
        String menuType,
        String path,
        String component,
        String perms,
        String icon,
        Integer sort,
        Integer visible,
        Integer status,
        LocalDateTime createTime,
        List<MenuVO> children
) {
}
