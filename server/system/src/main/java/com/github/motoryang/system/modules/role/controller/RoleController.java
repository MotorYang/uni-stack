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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final IRoleService roleService;

    /**
     * 分页查询角色
     */
    @GetMapping
    public RestResult<IPage<RoleVO>> page(RoleQueryDTO dto) {
        return RestResult.ok(roleService.pageQuery(dto));
    }

    /**
     * 获取所有角色（下拉选择用）
     */
    @GetMapping("/all")
    public RestResult<List<RoleVO>> listAll() {
        return RestResult.ok(roleService.listAll());
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    public RestResult<RoleDetailVO> getDetail(@PathVariable String id) {
        return RestResult.ok(roleService.getRoleDetail(id));
    }

    /**
     * 创建角色
     */
    @PostMapping
    public RestResult<RoleVO> create(@Valid @RequestBody RoleCreateDTO dto) {
        return RestResult.ok(roleService.createRole(dto));
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    public RestResult<RoleVO> update(@PathVariable String id, @Valid @RequestBody RoleUpdateDTO dto) {
        return RestResult.ok(roleService.updateRole(id, dto));
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public RestResult<Void> delete(@PathVariable String id) {
        roleService.deleteRole(id);
        return RestResult.ok();
    }

    /**
     * 获取特定角色下的所有用户
     */
    @GetMapping("/{roleId}/users")
    public RestResult<IPage<UserVO>> getUsers(@PathVariable String roleId, RoleUserQueryDTO dto) {
        RoleUserQueryDTO queryDTO = new RoleUserQueryDTO(
                roleId,
                dto.username(),
                dto.nickname(),
                dto.current(),
                dto.size()
        );
        return RestResult.ok(roleService.pageUserByRoleId(queryDTO));
    }

    @PostMapping("/{roleId}/users")
    public RestResult<Void> addUsers(@PathVariable String roleId, @RequestBody List<String> userIds) {
        roleService.addUsersToRole(roleId, userIds);
        return RestResult.ok();
    }

    @DeleteMapping("/{roleId}/users/{userId}")
    public RestResult<Void> removeUser(@PathVariable String roleId, @PathVariable String userId) {
        roleService.removeUserFromRole(roleId, userId);
        return RestResult.ok();
    }

    @DeleteMapping("/{roleId}/users")
    public RestResult<Void> removeUsers(@PathVariable String roleId, @RequestBody List<String> userIds) {
        roleService.removeUsersFromRole(roleId, userIds);
        return RestResult.ok();
    }
}
