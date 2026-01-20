package com.github.motoryang.system.modules.relation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.motoryang.system.modules.relation.entity.RoleMenu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色菜单关联 Mapper
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    /**
     * 批量插入角色菜单关联
     */
    int insertBatch(@Param("list") List<RoleMenu> list);

    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") String roleId);

    @Delete("DELETE FROM sys_role_menu WHERE menu_id = #{menuId}")
    int deleteByMenuId(@Param("menuId") String menuId);

    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<String> selectMenuIdsByRoleId(@Param("roleId") String roleId);

    /**
     * 根据用户ID查询权限编码列表
     */
    @Select("""
            SELECT DISTINCT m.perms FROM sys_menu m
            INNER JOIN sys_role_menu rm ON m.id = rm.menu_id
            INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND m.perms IS NOT NULL AND m.perms != ''
              AND m.status = 0 AND m.deleted = 0
            """)
    List<String> selectPermsByUserId(@Param("userId") String userId);
}
