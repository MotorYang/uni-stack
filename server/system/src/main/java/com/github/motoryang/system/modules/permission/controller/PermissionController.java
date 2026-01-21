package com.github.motoryang.system.modules.permission.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.system.modules.permission.model.dto.PermissionCreateDTO;
import com.github.motoryang.system.modules.permission.model.dto.PermissionQueryDTO;
import com.github.motoryang.system.modules.permission.model.dto.PermissionUpdateDTO;
import com.github.motoryang.system.modules.permission.model.vo.PermissionVO;
import com.github.motoryang.system.modules.permission.service.IPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 */
@Tag(name = "权限管理", description = "权限的增删改查")
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final IPermissionService permissionService;

    @Operation(summary = "分页查询权限列表")
    @GetMapping
    public RestResult<IPage<PermissionVO>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            PermissionQueryDTO query) {
        return RestResult.ok(permissionService.page(pageNum, pageSize, query));
    }

    @Operation(summary = "查询所有权限列表")
    @GetMapping("/list")
    public RestResult<List<PermissionVO>> list(PermissionQueryDTO query) {
        return RestResult.ok(permissionService.list(query));
    }

    @Operation(summary = "获取权限详情")
    @GetMapping("/{id}")
    public RestResult<PermissionVO> getById(
            @Parameter(description = "权限ID") @PathVariable String id) {
        return RestResult.ok(permissionService.getById(id));
    }

    @Operation(summary = "创建权限")
    @PostMapping
    public RestResult<String> create(@Valid @RequestBody PermissionCreateDTO dto) {
        return RestResult.ok(permissionService.create(dto));
    }

    @Operation(summary = "更新权限")
    @PutMapping("/{id}")
    public RestResult<Void> update(
            @Parameter(description = "权限ID") @PathVariable String id,
            @Valid @RequestBody PermissionUpdateDTO dto) {
        permissionService.update(id, dto);
        return RestResult.ok();
    }

    @Operation(summary = "删除权限")
    @DeleteMapping("/{id}")
    public RestResult<Void> delete(
            @Parameter(description = "权限ID") @PathVariable String id) {
        permissionService.delete(id);
        return RestResult.ok();
    }

    @Operation(summary = "批量删除权限")
    @DeleteMapping("/batch")
    public RestResult<Void> deleteBatch(@RequestBody List<String> ids) {
        permissionService.deleteBatch(ids);
        return RestResult.ok();
    }

    @Operation(summary = "获取权限关联的资源ID列表")
    @GetMapping("/{permissionId}/resources")
    public RestResult<List<String>> getPermissionResources(@Parameter(description = "权限ID") @PathVariable String permissionId) {
        return RestResult.ok(permissionService.getResourcesByPermissionId(permissionId));
    }

    @Operation(summary = "关联资源到权限")
    @PostMapping("/{permissionId}/resources")
    public RestResult<Void> assignResourcesToPermission(
            @Parameter(description = "权限ID") @PathVariable String permissionId,
            @Parameter(description = "资源ID列表") @RequestBody List<String> resourceIds) {
        permissionService.assignResourcesToPermission(permissionId, resourceIds);
        return RestResult.ok();
    }
}
