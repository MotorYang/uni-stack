package com.github.motoryang.system.modules.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.motoryang.system.modules.role.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    List<String> selectPermissionsByUserId(@Param("userId") String userId);

    List<Role> selectRolesByUserId(@Param("userId") String userId);
}
