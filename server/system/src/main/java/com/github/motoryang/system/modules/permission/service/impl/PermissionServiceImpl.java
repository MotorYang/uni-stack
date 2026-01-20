package com.github.motoryang.system.modules.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.motoryang.common.core.exception.BusinessException;
import com.github.motoryang.common.core.result.ResultCode;
import com.github.motoryang.system.modules.permission.converter.PermissionConverter;
import com.github.motoryang.system.modules.permission.entity.Permission;
import com.github.motoryang.system.modules.permission.mapper.PermissionMapper;
import com.github.motoryang.system.modules.permission.model.dto.PermissionCreateDTO;
import com.github.motoryang.system.modules.permission.model.dto.PermissionQueryDTO;
import com.github.motoryang.system.modules.permission.model.dto.PermissionUpdateDTO;
import com.github.motoryang.system.modules.permission.model.vo.PermissionVO;
import com.github.motoryang.system.modules.permission.service.IPermissionService;
import com.github.motoryang.system.modules.relation.mapper.RolePermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 权限服务实现
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements IPermissionService {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionConverter permissionConverter;

    @Override
    public IPage<PermissionVO> page(int pageNum, int pageSize, PermissionQueryDTO query) {
        LambdaQueryWrapper<Permission> wrapper = buildQueryWrapper(query);
        wrapper.orderByAsc(Permission::getSort);

        IPage<Permission> page = permissionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return page.convert(permissionConverter::toVO);
    }

    @Override
    public List<PermissionVO> list(PermissionQueryDTO query) {
        LambdaQueryWrapper<Permission> wrapper = buildQueryWrapper(query);
        wrapper.orderByAsc(Permission::getSort);

        List<Permission> list = permissionMapper.selectList(wrapper);
        return permissionConverter.toVOList(list);
    }

    @Override
    public PermissionVO getById(String id) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return permissionConverter.toVO(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(PermissionCreateDTO dto) {
        // 检查权限编码是否已存在
        if (isPermCodeExists(dto.permCode(), null)) {
            throw new BusinessException(400, "权限编码已存在");
        }

        Permission permission = permissionConverter.toEntity(dto);
        permission.setStatus(0);
        permissionMapper.insert(permission);
        return permission.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String id, PermissionUpdateDTO dto) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 检查权限编码是否已存在（排除自身）
        if (isPermCodeExists(dto.permCode(), id)) {
            throw new BusinessException(400, "权限编码已存在");
        }

        permissionConverter.updateEntity(dto, permission);
        permissionMapper.updateById(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 删除角色权限关联
        rolePermissionMapper.deleteByPermissionId(id);
        // 逻辑删除权限
        permissionMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<String> ids) {
        for (String id : ids) {
            rolePermissionMapper.deleteByPermissionId(id);
        }
        permissionMapper.deleteBatchIds(ids);
    }

    @Override
    public List<String> getPermCodesByUserId(String userId) {
        return permissionMapper.selectPermCodesByUserId(userId);
    }

    private LambdaQueryWrapper<Permission> buildQueryWrapper(PermissionQueryDTO query) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.like(StringUtils.hasText(query.permName()), Permission::getPermName, query.permName())
                    .like(StringUtils.hasText(query.permCode()), Permission::getPermCode, query.permCode())
                    .eq(query.status() != null, Permission::getStatus, query.status());
        }
        return wrapper;
    }

    private boolean isPermCodeExists(String permCode, String excludeId) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getPermCode, permCode);
        if (StringUtils.hasText(excludeId)) {
            wrapper.ne(Permission::getId, excludeId);
        }
        return permissionMapper.selectCount(wrapper) > 0;
    }
}
