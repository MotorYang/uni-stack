package com.github.motoryang.system.modules.relation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.motoryang.system.modules.relation.entity.UserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联 Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 批量插入用户角色关联
     */
    int insertBatch(@Param("list") List<UserRole> list);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") String userId);

    @Delete("DELETE FROM sys_user_role WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") String roleId);

    @Delete("DELETE FROM sys_user_role WHERE role_id = #{roleId} AND user_id = #{userId}")
    int deleteByRoleIdAndUserId(@Param("roleId") String roleId, @Param("userId") String userId);

    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<String> selectRoleIdsByUserId(@Param("userId") String userId);
}
