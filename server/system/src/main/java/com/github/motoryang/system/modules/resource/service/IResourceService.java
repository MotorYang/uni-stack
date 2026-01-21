package com.github.motoryang.system.modules.resource.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.motoryang.system.modules.resource.model.dto.ResourceCreateDTO;
import com.github.motoryang.system.modules.resource.model.dto.ResourceQueryDTO;
import com.github.motoryang.system.modules.resource.model.dto.ResourceUpdateDTO;
import com.github.motoryang.system.modules.resource.model.vo.ResourceVO;

import java.util.List;

/**
 * 资源服务接口
 */
public interface IResourceService {

    /**
     * 分页查询资源列表
     */
    IPage<ResourceVO> page(ResourceQueryDTO query);

    /**
     * 根据ID获取资源详情
     */
    ResourceVO getById(String id);

    /**
     * 创建资源
     */
    String create(ResourceCreateDTO dto);

    /**
     * 更新资源
     */
    void update(String id, ResourceUpdateDTO dto);

    /**
     * 删除资源
     */
    void delete(String id);

    /**
     * 批量删除资源
     */
    void deleteBatch(List<String> ids);

    /**
     * 统计资源组下的资源数量
     */
    Long countByGroupId(String groupId);
}
