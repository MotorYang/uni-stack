package com.github.motoryang.system.modules.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.motoryang.common.core.exception.BusinessException;
import com.github.motoryang.common.core.result.ResultCode;
import com.github.motoryang.system.modules.resource.converter.ResourceGroupConverter;
import com.github.motoryang.system.modules.resource.entity.ResourceGroup;
import com.github.motoryang.system.modules.resource.mapper.ResourceGroupMapper;
import com.github.motoryang.system.modules.resource.mapper.ResourceMapper;
import com.github.motoryang.system.modules.resource.model.dto.ResourceGroupCreateDTO;
import com.github.motoryang.system.modules.resource.model.dto.ResourceGroupQueryDTO;
import com.github.motoryang.system.modules.resource.model.dto.ResourceGroupUpdateDTO;
import com.github.motoryang.system.modules.resource.model.vo.ResourceGroupVO;
import com.github.motoryang.system.modules.resource.service.IResourceGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 资源组服务实现
 */
@Service
@RequiredArgsConstructor
public class ResourceGroupServiceImpl implements IResourceGroupService {

    private final ResourceGroupMapper resourceGroupMapper;
    private final ResourceMapper resourceMapper;
    private final ResourceGroupConverter resourceGroupConverter;

    @Override
    public List<ResourceGroupVO> list(ResourceGroupQueryDTO query) {
        LambdaQueryWrapper<ResourceGroup> wrapper = buildQueryWrapper(query);
        wrapper.orderByAsc(ResourceGroup::getSort)
                .orderByDesc(ResourceGroup::getCreateTime);

        List<ResourceGroup> list = resourceGroupMapper.selectList(wrapper);

        // 转换为 VO 并附加资源数量
        return list.stream().map(group -> {
            ResourceGroupVO vo = resourceGroupConverter.toVO(group);
            Long count = resourceMapper.countByGroupId(group.getId());
            return new ResourceGroupVO(
                    vo.id(),
                    vo.resGroupName(),
                    vo.resGroupCode(),
                    vo.description(),
                    vo.serviceName(),
                    vo.sort(),
                    vo.status(),
                    vo.createTime(),
                    count
            );
        }).toList();
    }

    @Override
    public ResourceGroupVO getById(String id) {
        ResourceGroup resourceGroup = resourceGroupMapper.selectById(id);
        if (resourceGroup == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        ResourceGroupVO vo = resourceGroupConverter.toVO(resourceGroup);
        Long count = resourceMapper.countByGroupId(id);
        return new ResourceGroupVO(
                vo.id(),
                vo.resGroupName(),
                vo.resGroupCode(),
                vo.description(),
                vo.serviceName(),
                vo.sort(),
                vo.status(),
                vo.createTime(),
                count
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(ResourceGroupCreateDTO dto) {
        // 检查编码是否已存在
        if (isCodeExists(dto.resGroupCode(), null)) {
            throw new BusinessException(400, "资源组编码已存在");
        }

        ResourceGroup resourceGroup = resourceGroupConverter.toEntity(dto);
        resourceGroup.setStatus(0);
        resourceGroupMapper.insert(resourceGroup);
        return resourceGroup.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String id, ResourceGroupUpdateDTO dto) {
        ResourceGroup resourceGroup = resourceGroupMapper.selectById(id);
        if (resourceGroup == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        resourceGroupConverter.updateEntity(dto, resourceGroup);
        resourceGroupMapper.updateById(resourceGroup);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        ResourceGroup resourceGroup = resourceGroupMapper.selectById(id);
        if (resourceGroup == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 检查是否有关联资源
        Long count = resourceMapper.countByGroupId(id);
        if (count > 0) {
            throw new BusinessException(400, "该资源组下存在资源，无法删除");
        }

        resourceGroupMapper.deleteById(id);
    }

    private LambdaQueryWrapper<ResourceGroup> buildQueryWrapper(ResourceGroupQueryDTO query) {
        LambdaQueryWrapper<ResourceGroup> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.like(StringUtils.hasText(query.resGroupName()), ResourceGroup::getResGroupName, query.resGroupName())
                    .like(StringUtils.hasText(query.resGroupCode()), ResourceGroup::getResGroupCode, query.resGroupCode())
                    .eq(StringUtils.hasText(query.serviceName()), ResourceGroup::getServiceName, query.serviceName())
                    .eq(query.status() != null, ResourceGroup::getStatus, query.status());
        }
        return wrapper;
    }

    private boolean isCodeExists(String code, String excludeId) {
        LambdaQueryWrapper<ResourceGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceGroup::getResGroupCode, code);
        if (StringUtils.hasText(excludeId)) {
            wrapper.ne(ResourceGroup::getId, excludeId);
        }
        return resourceGroupMapper.selectCount(wrapper) > 0;
    }
}
