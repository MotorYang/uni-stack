package com.github.motoryang.system.modules.role.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.motoryang.system.modules.role.entity.Role;
import com.github.motoryang.system.modules.role.model.dto.RoleCreateDTO;
import com.github.motoryang.system.modules.role.model.dto.RoleQueryDTO;
import com.github.motoryang.system.modules.role.model.dto.RoleUpdateDTO;
import com.github.motoryang.system.modules.role.model.vo.RoleDetailVO;
import com.github.motoryang.system.modules.role.model.vo.RoleVO;

import java.util.List;

/**
 * 角色服务接口
 */
public interface IRoleService extends IService<Role> {

    /**
     * 分页查询角色
     */
    IPage<RoleVO> pageQuery(RoleQueryDTO dto);

    /**
     * 获取所有角色（下拉选择用）
     */
    List<RoleVO> listAll();

    RoleDetailVO getRoleDetail(String id);

    /**
     * 创建角色
     */
    RoleVO createRole(RoleCreateDTO dto);

    RoleVO updateRole(String id, RoleUpdateDTO dto);

    void deleteRole(String id);

    /**
     * 检查角色Key是否存在
     */
    boolean existsByRoleKey(String roleKey);
}
