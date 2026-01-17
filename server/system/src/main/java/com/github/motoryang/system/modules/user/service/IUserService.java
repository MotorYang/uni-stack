package com.github.motoryang.system.modules.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.motoryang.system.modules.user.entity.User;
import com.github.motoryang.system.modules.user.model.dto.UserCreateDTO;
import com.github.motoryang.system.modules.user.model.dto.UserQueryDTO;
import com.github.motoryang.system.modules.user.model.dto.UserUpdateDTO;
import com.github.motoryang.system.modules.user.model.vo.UserDetailVO;
import com.github.motoryang.system.modules.user.model.vo.UserVO;

/**
 * 用户服务接口
 */
public interface IUserService extends IService<User> {

    /**
     * 分页查询用户
     */
    IPage<UserVO> pageQuery(UserQueryDTO dto);

    UserDetailVO getUserDetail(String id);

    UserVO createUser(UserCreateDTO dto);

    UserVO updateUser(String id, UserUpdateDTO dto);

    void deleteUser(String id);

    void resetPassword(String id, String newPassword);

    /**
     * 根据用户名获取用户（用于认证）
     */
    User getByUsername(String username);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
}
