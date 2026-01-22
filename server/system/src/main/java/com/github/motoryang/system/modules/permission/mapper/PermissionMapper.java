package com.github.motoryang.system.modules.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.motoryang.system.modules.permission.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限 Mapper
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据用户ID查询权限编码列表
     */
    @Select("""
            SELECT DISTINCT p.perm_code FROM sys_permission p
            INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
            INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND p.status = 0 AND p.deleted = 0
            """)
    List<String> selectPermCodesByUserId(@Param("userId") String userId);

    /**
     * 根据角色ID查询权限编码列表
     */
    @Select("""
            SELECT p.perm_code FROM sys_permission p
            INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
            WHERE rp.role_id = #{roleId}
              AND p.status = 0 AND p.deleted = 0
            """)
    List<String> selectPermCodesByRoleId(@Param("roleId") String roleId);

    /**
     * 根据角色ID查询权限实体列表
     */
    @Select("""
            SELECT p.* FROM sys_permission p
            INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
            WHERE rp.role_id = #{roleId}
              AND p.deleted = 0
            ORDER BY p.sort ASC
            """)
    List<Permission> selectByRoleId(@Param("roleId") String roleId);
}
