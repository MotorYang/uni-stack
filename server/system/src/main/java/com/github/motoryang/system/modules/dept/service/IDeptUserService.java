package com.github.motoryang.system.modules.dept.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.motoryang.system.modules.dept.model.dto.DeptAssignUsersDTO;
import com.github.motoryang.system.modules.dept.model.dto.DeptRemoveUsersDTO;
import com.github.motoryang.system.modules.dept.model.dto.DeptUserQueryDTO;
import com.github.motoryang.system.modules.dept.model.dto.SetPositionDTO;
import com.github.motoryang.system.modules.dept.model.dto.SetPrimaryDeptDTO;
import com.github.motoryang.system.modules.user.model.vo.UserVO;

/**
 * 部门用户服务接口
 */
public interface IDeptUserService {

    /**
     * 分页查询部门下的用户
     */
    IPage<UserVO> pageUsersByDept(DeptUserQueryDTO dto);

    /**
     * 分页查询未分配到指定部门的用户
     */
    IPage<UserVO> pageUnassignedUsers(DeptUserQueryDTO dto);

    /**
     * 部门分配用户
     */
    void assignUsers(DeptAssignUsersDTO dto);

    /**
     * 部门移除用户
     */
    void removeUsers(DeptRemoveUsersDTO dto);

    /**
     * 设置用户主职部门
     */
    void setPrimaryDept(SetPrimaryDeptDTO dto);

    /**
     * 设置用户在部门中的职务
     */
    void setPosition(SetPositionDTO dto);
}
