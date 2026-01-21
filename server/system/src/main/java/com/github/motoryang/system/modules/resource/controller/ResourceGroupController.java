package com.github.motoryang.system.modules.resource.controller;

import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.system.modules.resource.model.dto.ResourceGroupCreateDTO;
import com.github.motoryang.system.modules.resource.model.dto.ResourceGroupQueryDTO;
import com.github.motoryang.system.modules.resource.model.dto.ResourceGroupUpdateDTO;
import com.github.motoryang.system.modules.resource.model.vo.ResourceGroupVO;
import com.github.motoryang.system.modules.resource.service.IResourceGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资源组管理控制器
 */
@Tag(name = "资源组管理", description = "资源组的增删改查")
@RestController
@RequestMapping("/resource-groups")
@RequiredArgsConstructor
public class ResourceGroupController {

    private final IResourceGroupService resourceGroupService;

    @Operation(summary = "查询资源组列表")
    @GetMapping
    public RestResult<List<ResourceGroupVO>> list(ResourceGroupQueryDTO query) {
        return RestResult.ok(resourceGroupService.list(query));
    }

    @Operation(summary = "获取资源组详情")
    @GetMapping("/{id}")
    public RestResult<ResourceGroupVO> getById(
            @Parameter(description = "资源组ID") @PathVariable String id) {
        return RestResult.ok(resourceGroupService.getById(id));
    }

    @Operation(summary = "创建资源组")
    @PostMapping
    public RestResult<String> create(@Valid @RequestBody ResourceGroupCreateDTO dto) {
        return RestResult.ok(resourceGroupService.create(dto));
    }

    @Operation(summary = "更新资源组")
    @PutMapping("/{id}")
    public RestResult<Void> update(
            @Parameter(description = "资源组ID") @PathVariable String id,
            @Valid @RequestBody ResourceGroupUpdateDTO dto) {
        resourceGroupService.update(id, dto);
        return RestResult.ok();
    }

    @Operation(summary = "删除资源组")
    @DeleteMapping("/{id}")
    public RestResult<Void> delete(
            @Parameter(description = "资源组ID") @PathVariable String id) {
        resourceGroupService.delete(id);
        return RestResult.ok();
    }
}
