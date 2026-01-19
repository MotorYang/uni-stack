package com.github.motoryang.system.modules.dept.controller;

import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.system.modules.dept.model.dto.DeptCreateDTO;
import com.github.motoryang.system.modules.dept.model.dto.DeptQueryDTO;
import com.github.motoryang.system.modules.dept.model.dto.DeptUpdateDTO;
import com.github.motoryang.system.modules.dept.model.vo.DeptVO;
import com.github.motoryang.system.modules.dept.service.IDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 *
 * @author motoryang
 */
@Tag(name = "部门管理", description = "部门的增删改查、树形结构查询等操作")
@RestController
@RequestMapping("/depts")
@RequiredArgsConstructor
public class DeptController {

    private final IDeptService deptService;

    /**
     * 查询部门列表（树形）
     *
     * @param dto 查询条件
     * @return 部门树形列表
     */
    @Operation(summary = "查询部门列表", description = "查询部门列表，返回树形结构")
    @GetMapping
    public RestResult<List<DeptVO>> listTree(DeptQueryDTO dto) {
        return RestResult.ok(deptService.listTree(dto));
    }

    /**
     * 获取部门下拉列表（树形）
     *
     * @return 部门树形下拉列表
     */
    @Operation(summary = "获取部门树形下拉列表", description = "获取部门下拉选择数据，返回树形结构")
    @GetMapping("/tree-select")
    public RestResult<List<DeptVO>> getDeptTreeSelect() {
        return RestResult.ok(deptService.getDeptTreeSelect());
    }

    /**
     * 获取部门详情
     *
     * @param id 部门ID
     * @return 部门详情
     */
    @Operation(summary = "获取部门详情", description = "根据部门ID获取部门详细信息")
    @GetMapping("/{id}")
    public RestResult<DeptVO> getDetail(
            @Parameter(description = "部门ID", required = true) @PathVariable String id) {
        return RestResult.ok(deptService.getDeptDetail(id));
    }

    /**
     * 创建部门
     *
     * @param dto 部门创建参数
     * @return 创建的部门信息
     */
    @Operation(summary = "创建部门", description = "新增一个部门")
    @PostMapping
    public RestResult<DeptVO> create(@Valid @RequestBody DeptCreateDTO dto) {
        return RestResult.ok(deptService.createDept(dto));
    }

    /**
     * 更新部门
     *
     * @param id  部门ID
     * @param dto 部门更新参数
     * @return 更新后的部门信息
     */
    @Operation(summary = "更新部门", description = "根据部门ID更新部门信息")
    @PutMapping("/{id}")
    public RestResult<DeptVO> update(
            @Parameter(description = "部门ID", required = true) @PathVariable String id,
            @Valid @RequestBody DeptUpdateDTO dto) {
        return RestResult.ok(deptService.updateDept(id, dto));
    }

    /**
     * 删除部门
     *
     * @param id 部门ID
     * @return 操作结果
     */
    @Operation(summary = "删除部门", description = "根据部门ID删除部门（逻辑删除）")
    @DeleteMapping("/{id}")
    public RestResult<Void> delete(
            @Parameter(description = "部门ID", required = true) @PathVariable String id) {
        deptService.deleteDept(id);
        return RestResult.ok();
    }
}
