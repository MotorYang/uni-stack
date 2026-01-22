package com.github.motoryang.system.modules.permission.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.motoryang.system.modules.permission.model.dto.PermissionCreateDTO;
import com.github.motoryang.system.modules.permission.model.dto.PermissionQueryDTO;
import com.github.motoryang.system.modules.permission.model.dto.PermissionUpdateDTO;
import com.github.motoryang.system.modules.permission.model.vo.PermissionVO;

import java.util.List;

/**
 * 权限服务接口
 */
public interface IPermissionService {

    /**
     * 分页查询权限列表
     */
    IPage<PermissionVO> page(int pageNum, int pageSize, PermissionQueryDTO query);

    /**
     * 查询所有权限列表
     */
    List<PermissionVO> list(PermissionQueryDTO query);

    /**
     * 根据ID获取权限详情
     */
    PermissionVO getById(String id);

    /**
     * 创建权限
     */
    String create(PermissionCreateDTO dto);

    /**
     * 更新权限
     */
    void update(String id, PermissionUpdateDTO dto);

    /**
     * 删除权限
     */
    void delete(String id);

    /**
     * 批量删除权限
     */
    void deleteBatch(List<String> ids);

    /**
     * 根据用户ID查询权限编码列表
     */
    List<String> getPermCodesByUserId(String userId);

    /**
     * 关联资源到权限
     */
    void assignResourcesToPermission(String permissionId, List<String> resourceIds);

    /**
     * 根据权限ID查询关联的资源ID列表
     */
    List<String> getResourcesByPermissionId(String permissionId);

    /**
     * 根据角色ID查询权限列表
     */
    List<PermissionVO> getByRoleId(String roleId);
}
