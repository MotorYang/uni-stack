package com.github.motoryang.system.modules.menu.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 菜单创建 DTO
 */
public record MenuCreateDTO(
        String parentId,

        @NotBlank(message = "菜单名称不能为空")
        @Size(max = 50, message = "菜单名称长度不能超过50个字符")
        String menuName,

        @NotBlank(message = "菜单类型不能为空")
        String menuType,

        String path,

        String component,

        String perms,

        String icon,

        Integer sort
) {
    public MenuCreateDTO {
        if (parentId == null || parentId.isBlank()) {
            parentId = "0";
        }
        if (sort == null) {
            sort = 0;
        }
    }
}
