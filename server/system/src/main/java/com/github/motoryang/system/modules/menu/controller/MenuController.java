package com.github.motoryang.system.modules.menu.controller;

import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.system.modules.menu.model.dto.MenuCreateDTO;
import com.github.motoryang.system.modules.menu.model.dto.MenuQueryDTO;
import com.github.motoryang.system.modules.menu.model.dto.MenuUpdateDTO;
import com.github.motoryang.system.modules.menu.model.vo.MenuVO;
import com.github.motoryang.system.modules.menu.service.IMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 *
 * @author motoryang
 */
@Tag(name = "菜单管理", description = "菜单的增删改查、树形结构查询、角色菜单授权等操作")
@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final IMenuService menuService;

    /**
     * 查询菜单列表（树形）
     *
     * @param dto 查询条件
     * @return 菜单树形列表
     */
    @Operation(summary = "查询菜单列表", description = "查询菜单列表，返回树形结构")
    @GetMapping
    public RestResult<List<MenuVO>> listTree(MenuQueryDTO dto) {
        return RestResult.ok(menuService.listTree(dto));
    }

    /**
     * 获取用户菜单树
     *
     * @param userId 用户ID
     * @return 用户可访问的菜单树
     */
    @Operation(summary = "获取用户菜单树", description = "根据用户ID获取该用户可访问的菜单树")
    @GetMapping("/user/{userId}")
    public RestResult<List<MenuVO>> getUserMenuTree(
            @Parameter(description = "用户ID", required = true) @PathVariable String userId) {
        return RestResult.ok(menuService.getMenuTreeByUserId(userId));
    }

    /**
     * 获取菜单详情
     *
     * @param id 菜单ID
     * @return 菜单详情
     */
    @Operation(summary = "获取菜单详情", description = "根据菜单ID获取菜单详细信息")
    @GetMapping("/{id}")
    public RestResult<MenuVO> getDetail(
            @Parameter(description = "菜单ID", required = true) @PathVariable String id) {
        return RestResult.ok(menuService.getMenuDetail(id));
    }

    /**
     * 创建菜单
     *
     * @param dto 菜单创建参数
     * @return 创建的菜单信息
     */
    @Operation(summary = "创建菜单", description = "新增一个菜单")
    @PostMapping
    public RestResult<MenuVO> create(@Valid @RequestBody MenuCreateDTO dto) {
        return RestResult.ok(menuService.createMenu(dto));
    }

    /**
     * 更新菜单
     *
     * @param id  菜单ID
     * @param dto 菜单更新参数
     * @return 更新后的菜单信息
     */
    @Operation(summary = "更新菜单", description = "根据菜单ID更新菜单信息")
    @PutMapping("/{id}")
    public RestResult<MenuVO> update(
            @Parameter(description = "菜单ID", required = true) @PathVariable String id,
            @Valid @RequestBody MenuUpdateDTO dto) {
        return RestResult.ok(menuService.updateMenu(id, dto));
    }

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     * @return 操作结果
     */
    @Operation(summary = "删除菜单", description = "根据菜单ID删除菜单（逻辑删除）")
    @DeleteMapping("/{id}")
    public RestResult<Void> delete(
            @Parameter(description = "菜单ID", required = true) @PathVariable String id) {
        menuService.deleteMenu(id);
        return RestResult.ok();
    }

    /**
     * 获取角色菜单树（用于角色授权）
     *
     * @param roleId 角色ID
     * @return 角色已授权的菜单树
     */
    @Operation(summary = "获取角色菜单树", description = "获取指定角色已授权的菜单树，用于角色授权页面展示")
    @GetMapping("/role/{roleId}")
    public RestResult<List<MenuVO>> getRoleMenuTree(
            @Parameter(description = "角色ID", required = true) @PathVariable String roleId) {
        return RestResult.ok(menuService.getRoleMenuTree(roleId));
    }
}
