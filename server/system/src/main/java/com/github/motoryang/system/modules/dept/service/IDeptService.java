package com.github.motoryang.system.modules.dept.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.motoryang.system.modules.dept.entity.Dept;
import com.github.motoryang.system.modules.dept.model.dto.DeptCreateDTO;
import com.github.motoryang.system.modules.dept.model.dto.DeptQueryDTO;
import com.github.motoryang.system.modules.dept.model.dto.DeptUpdateDTO;
import com.github.motoryang.system.modules.dept.model.vo.DeptVO;

import java.util.List;

/**
 * 部门服务接口
 */
public interface IDeptService extends IService<Dept> {

    /**
     * 查询部门列表（树形）
     */
    List<DeptVO> listTree(DeptQueryDTO dto);

    /**
     * 获取部门详情
     */
    DeptVO getDeptDetail(String id);

    /**
     * 创建部门
     */
    DeptVO createDept(DeptCreateDTO dto);

    /**
     * 更新部门
     */
    DeptVO updateDept(String id, DeptUpdateDTO dto);

    /**
     * 删除部门
     */
    void deleteDept(String id);

    /**
     * 获取部门下拉列表（树形）
     */
    List<DeptVO> getDeptTreeSelect();
}
