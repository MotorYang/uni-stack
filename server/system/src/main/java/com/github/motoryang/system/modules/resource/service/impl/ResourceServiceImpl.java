package com.github.motoryang.system.modules.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.motoryang.common.core.exception.BusinessException;
import com.github.motoryang.common.core.result.ResultCode;
import com.github.motoryang.system.handler.PermissionCacheLoader;
import com.github.motoryang.system.modules.relation.mapper.PermissionResourceMapper;
import com.github.motoryang.system.modules.resource.converter.ResourceConverter;
import com.github.motoryang.system.modules.resource.entity.Resource;
import com.github.motoryang.system.modules.resource.mapper.ResourceGroupMapper;
import com.github.motoryang.system.modules.resource.mapper.ResourceMapper;
import com.github.motoryang.system.modules.resource.model.dto.ResourceCreateDTO;
import com.github.motoryang.system.modules.resource.model.dto.ResourceQueryDTO;
import com.github.motoryang.system.modules.resource.model.dto.ResourceUpdateDTO;
import com.github.motoryang.system.modules.resource.model.vo.ResourceVO;
import com.github.motoryang.system.modules.resource.service.IResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 资源服务实现
 */
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements IResourceService {

    private final ResourceMapper resourceMapper;
    private final ResourceGroupMapper resourceGroupMapper;
    private final ResourceConverter resourceConverter;
    private final PermissionResourceMapper permissionResourceMapper;
    private final PermissionCacheLoader cacheLoader;

    @Override
    public IPage<ResourceVO> page(ResourceQueryDTO query) {
        LambdaQueryWrapper<Resource> wrapper = buildQueryWrapper(query);
        wrapper.orderByAsc(Resource::getSort)
                .orderByDesc(Resource::getCreateTime);

        IPage<Resource> page = resourceMapper.selectPage(
                new Page<>(query.current(), query.size()),
                wrapper
        );
        return page.convert(resourceConverter::toVO);
    }

    @Override
    public ResourceVO getById(String id) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return resourceConverter.toVO(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(ResourceCreateDTO dto) {
        // 检查资源组是否存在
        if (resourceGroupMapper.selectById(dto.groupId()) == null) {
            throw new BusinessException(400, "资源组不存在");
        }

        // 检查资源编码是否已存在
        if (isCodeExists(dto.resCode(), null)) {
            throw new BusinessException(400, "资源编码已存在");
        }

        Resource resource = resourceConverter.toEntity(dto);
        resource.setStatus(0);
        if (resource.getResMethod() == null) {
            resource.setResMethod("*");
        }
        resourceMapper.insert(resource);
        return resource.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String id, ResourceUpdateDTO dto) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        resourceConverter.updateEntity(dto, resource);
        resourceMapper.updateById(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        resourceMapper.deleteById(id);
        // 删除权限资源关联
        permissionResourceMapper.deleteByResourceId(id);
        cacheLoader.refreshCacheAfterCommit();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<String> ids) {
        resourceMapper.deleteByIds(ids);
        // 删除权限资源关联
        ids.forEach(permissionResourceMapper::deleteByResourceId);
        cacheLoader.refreshCacheAfterCommit();
    }

    @Override
    public Long countByGroupId(String groupId) {
        return resourceMapper.countByGroupId(groupId);
    }

    private LambdaQueryWrapper<Resource> buildQueryWrapper(ResourceQueryDTO query) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.eq(StringUtils.hasText(query.groupId()), Resource::getGroupId, query.groupId())
                    .like(StringUtils.hasText(query.resName()), Resource::getResName, query.resName())
                    .eq(StringUtils.hasText(query.resType()), Resource::getResType, query.resType())
                    .like(StringUtils.hasText(query.resCode()), Resource::getResCode, query.resCode())
                    .eq(query.status() != null, Resource::getStatus, query.status());
        }
        return wrapper;
    }

    private boolean isCodeExists(String code, String excludeId) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resource::getResCode, code);
        if (StringUtils.hasText(excludeId)) {
            wrapper.ne(Resource::getId, excludeId);
        }
        return resourceMapper.selectCount(wrapper) > 0;
    }
}
