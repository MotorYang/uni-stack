package com.github.motoryang.system.modules.resource.service;

import com.github.motoryang.system.modules.resource.model.dto.ResourceGroupCreateDTO;
import com.github.motoryang.system.modules.resource.model.dto.ResourceGroupQueryDTO;
import com.github.motoryang.system.modules.resource.model.dto.ResourceGroupUpdateDTO;
import com.github.motoryang.system.modules.resource.model.vo.ResourceGroupVO;

import java.util.List;

/**
 * 资源组服务接口
 */
public interface IResourceGroupService {

    /**
     * 查询所有资源组列表
     */
    List<ResourceGroupVO> list(ResourceGroupQueryDTO query);

    /**
     * 根据ID获取资源组详情
     */
    ResourceGroupVO getById(String id);

    /**
     * 创建资源组
     */
    String create(ResourceGroupCreateDTO dto);

    /**
     * 更新资源组
     */
    void update(String id, ResourceGroupUpdateDTO dto);

    /**
     * 删除资源组
     */
    void delete(String id);
}
