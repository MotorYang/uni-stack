package com.github.motoryang.system.modules.menu.model.dto;

/**
 * 菜单查询 DTO
 */
public record MenuQueryDTO(
        String menuName,
        String menuType,
        Integer status
) {
}
