package com.github.motoryang.system.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.motoryang.system.modules.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 分页查询用户（包含部门名称）
     */
    IPage<User> selectUserPage(IPage<User> page, @Param("user") User user);

    /**
     * 分页查询指定角色下的用户
     */
    IPage<User> selectUsersByRoleId(IPage<User> page,
                                    @Param("roleId") String roleId,
                                    @Param("username") String username,
                                    @Param("nickname") String nickname);

    /**
     * 分页查询未分配指定角色的用户（排除已禁用用户）
     */
    IPage<User> selectUnassignedUsersByRoleId(IPage<User> page,
                                              @Param("roleId") String roleId,
                                              @Param("username") String username,
                                              @Param("nickname") String nickname);

    User selectUserById(@Param("id") String id);

    /**
     * 分页查询指定部门下的用户
     */
    IPage<User> selectUsersByDeptId(IPage<User> page,
                                    @Param("deptId") String deptId,
                                    @Param("username") String username,
                                    @Param("nickname") String nickname,
                                    @Param("status") Integer status);

    /**
     * 分页查询未分配到指定部门的用户
     */
    IPage<User> selectUnassignedUsersByDeptId(IPage<User> page,
                                              @Param("deptId") String deptId,
                                              @Param("username") String username,
                                              @Param("nickname") String nickname);
}
