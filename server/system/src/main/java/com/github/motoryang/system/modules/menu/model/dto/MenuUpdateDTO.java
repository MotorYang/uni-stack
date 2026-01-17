package com.github.motoryang.system.modules.menu.model.dto;

import jakarta.validation.constraints.Size;

/**
 * 菜单更新 DTO
 */
public record MenuUpdateDTO(
        String parentId,

        @Size(max = 50, message = "菜单名称长度不能超过50个字符")
        String menuName,

        String menuType,

        String path,

        String component,

        String perms,

        String icon,

        Integer sort,

        Integer visible,

        Integer status
) {
}
