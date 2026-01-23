package com.github.motoryang.system.modules.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.motoryang.common.core.exception.BusinessException;
import com.github.motoryang.system.handler.PermissionCacheLoader;
import com.github.motoryang.system.modules.relation.entity.RoleMenu;
import com.github.motoryang.system.modules.relation.entity.RolePermission;
import com.github.motoryang.system.modules.relation.entity.UserRole;
import com.github.motoryang.system.modules.relation.mapper.RoleMenuMapper;
import com.github.motoryang.system.modules.relation.mapper.RolePermissionMapper;
import com.github.motoryang.system.modules.relation.mapper.UserRoleMapper;
import com.github.motoryang.system.modules.role.converter.RoleConverter;
import com.github.motoryang.system.modules.role.entity.Role;
import com.github.motoryang.system.modules.role.mapper.RoleMapper;
import com.github.motoryang.system.modules.role.model.dto.RoleCreateDTO;
import com.github.motoryang.system.modules.role.model.dto.RoleQueryDTO;
import com.github.motoryang.system.modules.role.model.dto.RoleUpdateDTO;
import com.github.motoryang.system.modules.role.model.dto.RoleUserQueryDTO;
import com.github.motoryang.system.modules.role.model.vo.RoleDetailVO;
import com.github.motoryang.system.modules.role.model.vo.RoleVO;
import com.github.motoryang.system.modules.role.service.IRoleService;
import com.github.motoryang.system.modules.user.converter.UserConverter;
import com.github.motoryang.system.modules.user.entity.User;
import com.github.motoryang.system.modules.user.mapper.UserMapper;
import com.github.motoryang.system.modules.user.model.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 角色服务实现
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    private final RoleConverter roleConverter;
    private final RoleMenuMapper roleMenuMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserMapper userMapper;
    private final UserConverter userConverter;
    private final UserRoleMapper userRoleMapper;
    private final PermissionCacheLoader cacheLoader;

    @Override
    public IPage<RoleVO> pageQuery(RoleQueryDTO dto) {
        Page<Role> page = new Page<>(dto.current(), dto.size());

        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.roleName()), Role::getRoleName, dto.roleName())
                .like(StringUtils.hasText(dto.roleKey()), Role::getRoleKey, dto.roleKey())
                .eq(dto.status() != null, Role::getStatus, dto.status())
                .orderByAsc(Role::getSort)
                .orderByDesc(Role::getCreateTime);

        page(page, wrapper);

        Page<RoleVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(roleConverter.toVOList(page.getRecords()));
        return voPage;
    }

    @Override
    public List<RoleVO> listAll() {
        List<Role> roles = list(new LambdaQueryWrapper<Role>()
                .eq(Role::getStatus, 0)
                .orderByAsc(Role::getSort));
        return roleConverter.toVOList(roles);
    }

    @Override
    public RoleDetailVO getRoleDetail(String id) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        List<String> menuIds = roleMenuMapper.selectMenuIdsByRoleId(id);
        List<String> permissionIds = rolePermissionMapper.selectPermissionIdsByRoleId(id);

        return new RoleDetailVO(
                role.getId(),
                role.getRoleName(),
                role.getRoleKey(),
                role.getSort(),
                role.getStatus(),
                role.getRemark(),
                menuIds,
                permissionIds,
                role.getCreateTime(),
                role.getUpdateTime()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO createRole(RoleCreateDTO dto) {
        if (existsByRoleKey(dto.roleKey())) {
            throw new BusinessException("角色权限字符串已存在");
        }

        Role role = roleConverter.toEntity(dto);
        save(role);

        // 保存角色菜单关联
        if (!CollectionUtils.isEmpty(dto.menuIds())) {
            saveRoleMenus(role.getId(), dto.menuIds());
        }

        // 保存角色权限关联
        if (!CollectionUtils.isEmpty(dto.permissionIds())) {
            saveRolePermissions(role.getId(), dto.permissionIds());
        }

        return roleConverter.toVO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO updateRole(String id, RoleUpdateDTO dto) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 检查角色Key是否重复
        if (StringUtils.hasText(dto.roleKey()) && !dto.roleKey().equals(role.getRoleKey())) {
            if (existsByRoleKey(dto.roleKey())) {
                throw new BusinessException("角色权限字符串已存在");
            }
        }

        roleConverter.updateEntity(dto, role);
        updateById(role);

        // 更新角色菜单关联
        if (dto.menuIds() != null) {
            roleMenuMapper.deleteByRoleId(id);
            if (!dto.menuIds().isEmpty()) {
                saveRoleMenus(id, dto.menuIds());
            }
        }

        // 更新角色权限关联
        if (dto.permissionIds() != null) {
            rolePermissionMapper.deleteByRoleId(id);
            if (!dto.permissionIds().isEmpty()) {
                saveRolePermissions(id, dto.permissionIds());
            }
        }

        return roleConverter.toVO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(String id) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 删除角色菜单关联
        roleMenuMapper.deleteByRoleId(id);
        // 删除角色权限关联
        rolePermissionMapper.deleteByRoleId(id);

        // 逻辑删除角色
        removeById(id);
        // 刷新权限缓存
        cacheLoader.refreshCacheAfterCommit();
    }

    @Override
    public boolean existsByRoleKey(String roleKey) {
        return count(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleKey, roleKey)) > 0;
    }

    @Override
    public IPage<UserVO> pageUserByRoleId(RoleUserQueryDTO dto) {
        Page<User> page = new Page<>(dto.current(), dto.size());

        IPage<User> userPage = userMapper.selectUsersByRoleId(
                page,
                dto.roleId(),
                dto.username(),
                dto.nickname()
        );

        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userConverter.toVOList(userPage.getRecords()));
        return voPage;
    }

    @Override
    public IPage<UserVO> pageUnassignedUsersByRoleId(RoleUserQueryDTO dto) {
        Page<User> page = new Page<>(dto.current(), dto.size());

        IPage<User> userPage = userMapper.selectUnassignedUsersByRoleId(
                page,
                dto.roleId(),
                dto.username(),
                dto.nickname()
        );

        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userConverter.toVOList(userPage.getRecords()));
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUsersToRole(String roleId, List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        List<UserRole> userRoles = userIds.stream()
                .map(userId -> {
                    UserRole ur = new UserRole();
                    ur.setUserId(userId);
                    ur.setRoleId(roleId);
                    return ur;
                })
                .toList();
        userRoleMapper.insertBatch(userRoles);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUserFromRole(String roleId, String userId) {
        userRoleMapper.deleteByRoleIdAndUserId(roleId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUsersFromRole(String roleId, List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        userIds.forEach(userId -> userRoleMapper.deleteByRoleIdAndUserId(roleId, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPermissionsToRole(String roleId, List<String> permissionIds) {
        if (CollectionUtils.isEmpty(permissionIds)) {
            return;
        }
        // 获取已有的权限ID列表，避免重复添加
        List<String> existingIds = rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
        List<String> newIds = permissionIds.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();
        if (!newIds.isEmpty()) {
            saveRolePermissions(roleId, newIds);
            cacheLoader.refreshCacheAfterCommit();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removePermissionFromRole(String roleId, String permissionId) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId)
                .eq(RolePermission::getPermissionId, permissionId));
        cacheLoader.refreshCacheAfterCommit();
    }

    private void saveRoleMenus(String roleId, List<String> menuIds) {
        List<RoleMenu> roleMenus = menuIds.stream()
                .map(menuId -> {
                    RoleMenu rm = new RoleMenu();
                    rm.setRoleId(roleId);
                    rm.setMenuId(menuId);
                    return rm;
                })
                .toList();
        roleMenuMapper.insertBatch(roleMenus);
    }

    private void saveRolePermissions(String roleId, List<String> permissionIds) {
        List<RolePermission> rolePermissions = permissionIds.stream()
                .map(permissionId -> {
                    RolePermission rp = new RolePermission();
                    rp.setRoleId(roleId);
                    rp.setPermissionId(permissionId);
                    return rp;
                })
                .toList();
        rolePermissionMapper.insertBatch(rolePermissions);
    }
}
