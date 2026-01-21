package com.github.motoryang.system.modules.resource.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.system.modules.resource.model.dto.ResourceCreateDTO;
import com.github.motoryang.system.modules.resource.model.dto.ResourceQueryDTO;
import com.github.motoryang.system.modules.resource.model.dto.ResourceUpdateDTO;
import com.github.motoryang.system.modules.resource.model.vo.ResourceVO;
import com.github.motoryang.system.modules.resource.service.IResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资源管理控制器
 */
@Tag(name = "资源管理", description = "资源的增删改查")
@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final IResourceService resourceService;

    @Operation(summary = "分页查询资源列表")
    @GetMapping
    public RestResult<IPage<ResourceVO>> page(ResourceQueryDTO query) {
        return RestResult.ok(resourceService.page(query));
    }

    @Operation(summary = "获取资源详情")
    @GetMapping("/{id}")
    public RestResult<ResourceVO> getById(
            @Parameter(description = "资源ID") @PathVariable String id) {
        return RestResult.ok(resourceService.getById(id));
    }

    @Operation(summary = "创建资源")
    @PostMapping
    public RestResult<String> create(@Valid @RequestBody ResourceCreateDTO dto) {
        return RestResult.ok(resourceService.create(dto));
    }

    @Operation(summary = "更新资源")
    @PutMapping("/{id}")
    public RestResult<Void> update(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody ResourceUpdateDTO dto) {
        resourceService.update(id, dto);
        return RestResult.ok();
    }

    @Operation(summary = "删除资源")
    @DeleteMapping("/{id}")
    public RestResult<Void> delete(
            @Parameter(description = "资源ID") @PathVariable String id) {
        resourceService.delete(id);
        return RestResult.ok();
    }

    @Operation(summary = "批量删除资源")
    @DeleteMapping("/batch")
    public RestResult<Void> deleteBatch(@RequestBody List<String> ids) {
        resourceService.deleteBatch(ids);
        return RestResult.ok();
    }
}
