package com.github.motoryang.system.modules.relation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.motoryang.system.modules.relation.entity.PermissionResource;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限资源关联 Mapper
 */
@Mapper
public interface PermissionResourceMapper extends BaseMapper<PermissionResource> {

    /**
     * 批量插入权限资源关联
     */
    int insertBatch(@Param("list") List<PermissionResource> list);

    @Delete("DELETE FROM sys_permission_resource WHERE perm_id = #{permId}")
    int deleteByPermissionId(@Param("permId") String permId);

    @Delete("DELETE FROM sys_permission_resource WHERE res_id = #{resId}")
    int deleteByResourceId(@Param("resId") String resId);

    @Select("SELECT res_id FROM sys_permission_resource WHERE perm_id = #{permId}")
    List<String> selectResourceIdsByPermissionId(@Param("permId") String permId);
}
