package com.github.motoryang.system.modules.menu.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.motoryang.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 菜单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class Menu extends BaseEntity {

    /**
     * 父菜单ID
     */
    private String parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单类型（M目录 C菜单 F按钮）
     */
    private String menuType;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 显示状态（0显示 1隐藏）
     */
    private Integer visible;

    /**
     * 状态（0正常 1停用）
     */
    private Integer status;

    /**
     * 子菜单（非数据库字段）
     */
    @TableField(exist = false)
    private List<Menu> children;
}
