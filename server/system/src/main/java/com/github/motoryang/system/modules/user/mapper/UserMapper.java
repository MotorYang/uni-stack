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

    User selectUserById(@Param("id") String id);
}
