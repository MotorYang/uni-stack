package com.github.motoryang.system.modules.menu.controller;

import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.system.modules.menu.model.dto.MenuCreateDTO;
import com.github.motoryang.system.modules.menu.model.dto.MenuQueryDTO;
import com.github.motoryang.system.modules.menu.model.dto.MenuUpdateDTO;
import com.github.motoryang.system.modules.menu.model.vo.MenuVO;
import com.github.motoryang.system.modules.menu.service.IMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final IMenuService menuService;

    /**
     * 查询菜单列表（树形）
     */
    @GetMapping
    public RestResult<List<MenuVO>> listTree(MenuQueryDTO dto) {
        return RestResult.ok(menuService.listTree(dto));
    }

    /**
     * 获取用户菜单树
     */
    @GetMapping("/user/{userId}")
    public RestResult<List<MenuVO>> getUserMenuTree(@PathVariable String userId) {
        return RestResult.ok(menuService.getMenuTreeByUserId(userId));
    }

    /**
     * 获取菜单详情
     */
    @GetMapping("/{id}")
    public RestResult<MenuVO> getDetail(@PathVariable String id) {
        return RestResult.ok(menuService.getMenuDetail(id));
    }

    /**
     * 创建菜单
     */
    @PostMapping
    public RestResult<MenuVO> create(@Valid @RequestBody MenuCreateDTO dto) {
        return RestResult.ok(menuService.createMenu(dto));
    }

    /**
     * 更新菜单
     */
    @PutMapping("/{id}")
    public RestResult<MenuVO> update(@PathVariable String id, @Valid @RequestBody MenuUpdateDTO dto) {
        return RestResult.ok(menuService.updateMenu(id, dto));
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public RestResult<Void> delete(@PathVariable String id) {
        menuService.deleteMenu(id);
        return RestResult.ok();
    }

    /**
     * 获取角色菜单树（用于角色授权）
     */
    @GetMapping("/role/{roleId}")
    public RestResult<List<MenuVO>> getRoleMenuTree(@PathVariable String roleId) {
        return RestResult.ok(menuService.getRoleMenuTree(roleId));
    }
}
