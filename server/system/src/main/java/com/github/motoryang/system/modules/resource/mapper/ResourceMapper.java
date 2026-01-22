package com.github.motoryang.system.modules.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.motoryang.system.modules.resource.entity.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

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

    /**
     * 查询所有 API 资源及其关联的角色
     * 返回结果: apiPattern (如 GET:/api/user/**), roleKeys (如 ADMIN,USER)
     */
    @Select("""
            SELECT
                CONCAT(r.res_method, ':', r.res_path) AS apiPattern,
                GROUP_CONCAT(DISTINCT ro.role_key ORDER BY ro.role_key SEPARATOR ',') AS roleKeys
            FROM sys_resource r
            INNER JOIN sys_permission_resource pr ON r.id = pr.res_id
            INNER JOIN sys_permission p ON pr.perm_id = p.id AND p.status = 0 AND p.deleted = 0
            INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
            INNER JOIN sys_role ro ON rp.role_id = ro.id AND ro.status = 0 AND ro.deleted = 0
            WHERE r.res_type = 'API'
              AND r.status = 0
              AND r.deleted = 0
            GROUP BY r.res_method, r.res_path
            """)
    List<Map<String, String>> selectApiRoleMappings();
}
