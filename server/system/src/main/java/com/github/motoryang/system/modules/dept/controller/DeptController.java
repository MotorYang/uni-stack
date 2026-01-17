package com.github.motoryang.system.modules.dept.controller;

import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.system.modules.dept.model.dto.DeptCreateDTO;
import com.github.motoryang.system.modules.dept.model.dto.DeptQueryDTO;
import com.github.motoryang.system.modules.dept.model.dto.DeptUpdateDTO;
import com.github.motoryang.system.modules.dept.model.vo.DeptVO;
import com.github.motoryang.system.modules.dept.service.IDeptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 */
@RestController
@RequestMapping("/depts")
@RequiredArgsConstructor
public class DeptController {

    private final IDeptService deptService;

    /**
     * 查询部门列表（树形）
     */
    @GetMapping
    public RestResult<List<DeptVO>> listTree(DeptQueryDTO dto) {
        return RestResult.ok(deptService.listTree(dto));
    }

    /**
     * 获取部门下拉列表（树形）
     */
    @GetMapping("/tree-select")
    public RestResult<List<DeptVO>> getDeptTreeSelect() {
        return RestResult.ok(deptService.getDeptTreeSelect());
    }

    /**
     * 获取部门详情
     */
    @GetMapping("/{id}")
    public RestResult<DeptVO> getDetail(@PathVariable String id) {
        return RestResult.ok(deptService.getDeptDetail(id));
    }

    /**
     * 创建部门
     */
    @PostMapping
    public RestResult<DeptVO> create(@Valid @RequestBody DeptCreateDTO dto) {
        return RestResult.ok(deptService.createDept(dto));
    }

    /**
     * 更新部门
     */
    @PutMapping("/{id}")
    public RestResult<DeptVO> update(@PathVariable String id, @Valid @RequestBody DeptUpdateDTO dto) {
        return RestResult.ok(deptService.updateDept(id, dto));
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    public RestResult<Void> delete(@PathVariable String id) {
        deptService.deleteDept(id);
        return RestResult.ok();
    }
}
