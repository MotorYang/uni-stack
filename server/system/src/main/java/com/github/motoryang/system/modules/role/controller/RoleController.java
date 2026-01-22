package com.github.motoryang.system.modules.role.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.system.modules.role.model.dto.RoleCreateDTO;
import com.github.motoryang.system.modules.role.model.dto.RoleQueryDTO;
import com.github.motoryang.system.modules.role.model.dto.RoleUpdateDTO;
import com.github.motoryang.system.modules.role.model.dto.RoleUserQueryDTO;
import com.github.motoryang.system.modules.role.model.vo.RoleDetailVO;
import com.github.motoryang.system.modules.role.model.vo.RoleVO;
import com.github.motoryang.system.modules.role.service.IRoleService;
import com.github.motoryang.system.modules.user.model.vo.UserVO;
import com.github.motoryang.system.modules.permission.model.vo.PermissionVO;
import com.github.motoryang.system.modules.permission.service.IPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 *
 * @author motoryang
 */
@Tag(name = "角色管理", description = "角色的增删改查、角色用户管理等操作")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final IRoleService roleService;
    private final IPermissionService permissionService;

    /**
     * 分页查询角色
     *
     * @param dto 查询条件
     * @return 角色分页列表
     */
    @Operation(summary = "分页查询角色", description = "根据条件分页查询角色列表")
    @GetMapping
    public RestResult<IPage<RoleVO>> page(RoleQueryDTO dto) {
        return RestResult.ok(roleService.pageQuery(dto));
    }

    /**
     * 获取所有角色（下拉选择用）
     *
     * @return 所有角色列表
     */
    @Operation(summary = "获取所有角色", description = "获取所有角色列表，用于下拉选择")
    @GetMapping("/all")
    public RestResult<List<RoleVO>> listAll() {
        return RestResult.ok(roleService.listAll());
    }

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    @Operation(summary = "获取角色详情", description = "根据角色ID获取角色详细信息，包含已授权的菜单ID列表")
    @GetMapping("/{id}")
    public RestResult<RoleDetailVO> getDetail(
            @Parameter(description = "角色ID", required = true) @PathVariable String id) {
        return RestResult.ok(roleService.getRoleDetail(id));
    }

    /**
     * 创建角色
     *
     * @param dto 角色创建参数
     * @return 创建的角色信息
     */
    @Operation(summary = "创建角色", description = "新增一个角色")
    @PostMapping
    public RestResult<RoleVO> create(@Valid @RequestBody RoleCreateDTO dto) {
        return RestResult.ok(roleService.createRole(dto));
    }

    /**
     * 更新角色
     *
     * @param id  角色ID
     * @param dto 角色更新参数
     * @return 更新后的角色信息
     */
    @Operation(summary = "更新角色", description = "根据角色ID更新角色信息")
    @PutMapping("/{id}")
    public RestResult<RoleVO> update(
            @Parameter(description = "角色ID", required = true) @PathVariable String id,
            @Valid @RequestBody RoleUpdateDTO dto) {
        return RestResult.ok(roleService.updateRole(id, dto));
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 操作结果
     */
    @Operation(summary = "删除角色", description = "根据角色ID删除角色（逻辑删除）")
    @DeleteMapping("/{id}")
    public RestResult<Void> delete(
            @Parameter(description = "角色ID", required = true) @PathVariable String id) {
        roleService.deleteRole(id);
        return RestResult.ok();
    }

    /**
     * 获取特定角色下的所有用户
     *
     * @param roleId 角色ID
     * @param dto    查询条件
     * @return 用户分页列表
     */
    @Operation(summary = "获取角色下的用户", description = "分页查询指定角色下的所有用户")
    @GetMapping("/{roleId}/users")
    public RestResult<IPage<UserVO>> getUsers(
            @Parameter(description = "角色ID", required = true) @PathVariable String roleId,
            RoleUserQueryDTO dto) {
        RoleUserQueryDTO queryDTO = new RoleUserQueryDTO(
                roleId,
                dto.username(),
                dto.nickname(),
                dto.current(),
                dto.size()
        );
        return RestResult.ok(roleService.pageUserByRoleId(queryDTO));
    }

    /**
     * 批量添加用户到角色
     *
     * @param roleId  角色ID
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量添加用户到角色", description = "将多个用户添加到指定角色")
    @PostMapping("/{roleId}/users")
    public RestResult<Void> addUsers(
            @Parameter(description = "角色ID", required = true) @PathVariable String roleId,
            @RequestBody List<String> userIds) {
        roleService.addUsersToRole(roleId, userIds);
        return RestResult.ok();
    }

    /**
     * 从角色移除单个用户
     *
     * @param roleId 角色ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @Operation(summary = "从角色移除用户", description = "从指定角色移除单个用户")
    @DeleteMapping("/{roleId}/users/{userId}")
    public RestResult<Void> removeUser(
            @Parameter(description = "角色ID", required = true) @PathVariable String roleId,
            @Parameter(description = "用户ID", required = true) @PathVariable String userId) {
        roleService.removeUserFromRole(roleId, userId);
        return RestResult.ok();
    }

    /**
     * 批量从角色移除用户
     *
     * @param roleId  角色ID
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量从角色移除用户", description = "从指定角色批量移除多个用户")
    @DeleteMapping("/{roleId}/users")
    public RestResult<Void> removeUsers(
            @Parameter(description = "角色ID", required = true) @PathVariable String roleId,
            @RequestBody List<String> userIds) {
        roleService.removeUsersFromRole(roleId, userIds);
        return RestResult.ok();
    }

    /**
     * 获取角色关联的权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Operation(summary = "获取角色权限列表", description = "获取指定角色关联的所有权限")
    @GetMapping("/{roleId}/permissions")
    public RestResult<List<PermissionVO>> getPermissions(
            @Parameter(description = "角色ID", required = true) @PathVariable String roleId) {
        return RestResult.ok(permissionService.getByRoleId(roleId));
    }

    /**
     * 批量添加权限到角色
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量添加权限到角色", description = "将多个权限添加到指定角色")
    @PostMapping("/{roleId}/permissions")
    public RestResult<Void> addPermissions(
            @Parameter(description = "角色ID", required = true) @PathVariable String roleId,
            @RequestBody List<String> permissionIds) {
        roleService.addPermissionsToRole(roleId, permissionIds);
        return RestResult.ok();
    }

    /**
     * 从角色移除单个权限
     *
     * @param roleId       角色ID
     * @param permissionId 权限ID
     * @return 操作结果
     */
    @Operation(summary = "从角色移除权限", description = "从指定角色移除单个权限")
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public RestResult<Void> removePermission(
            @Parameter(description = "角色ID", required = true) @PathVariable String roleId,
            @Parameter(description = "权限ID", required = true) @PathVariable String permissionId) {
        roleService.removePermissionFromRole(roleId, permissionId);
        return RestResult.ok();
    }
}
