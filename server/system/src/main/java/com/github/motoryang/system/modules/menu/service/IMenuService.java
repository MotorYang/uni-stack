package com.github.motoryang.system.modules.menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.motoryang.system.modules.menu.entity.Menu;
import com.github.motoryang.system.modules.menu.model.dto.MenuCreateDTO;
import com.github.motoryang.system.modules.menu.model.dto.MenuQueryDTO;
import com.github.motoryang.system.modules.menu.model.dto.MenuUpdateDTO;
import com.github.motoryang.system.modules.menu.model.vo.MenuVO;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface IMenuService extends IService<Menu> {

    /**
     * 查询菜单列表（树形）
     */
    List<MenuVO> listTree(MenuQueryDTO dto);

    /**
     * 根据用户ID查询菜单树
     */
    List<MenuVO> getMenuTreeByUserId(String userId);

    /**
     * 获取菜单详情
     */
    MenuVO getMenuDetail(String id);

    /**
     * 创建菜单
     */
    MenuVO createMenu(MenuCreateDTO dto);

    /**
     * 更新菜单
     */
    MenuVO updateMenu(String id, MenuUpdateDTO dto);

    /**
     * 删除菜单
     */
    void deleteMenu(String id);

    /**
     * 获取角色菜单树（用于角色授权）
     */
    List<MenuVO> getRoleMenuTree(String roleId);
}
