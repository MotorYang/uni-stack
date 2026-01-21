package com.github.motoryang.system.modules.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.motoryang.system.modules.resource.entity.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 资源 Mapper
 */
@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {

    /**
     * 统计资源组下的资源数量
     */
    @Select("SELECT COUNT(*) FROM sys_resource WHERE group_id = #{groupId} AND deleted = 0")
    Long countByGroupId(@Param("groupId") String groupId);
}
