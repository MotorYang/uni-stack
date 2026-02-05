package com.github.motoryang.system.modules.notice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.system.modules.notice.model.dto.NoticeCreateDTO;
import com.github.motoryang.system.modules.notice.model.dto.NoticeQueryDTO;
import com.github.motoryang.system.modules.notice.model.dto.NoticeUpdateDTO;
import com.github.motoryang.system.modules.notice.model.vo.NoticeDetailVO;
import com.github.motoryang.system.modules.notice.model.vo.NoticeVO;
import com.github.motoryang.system.modules.notice.service.INoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 通知管理控制器
 *
 * @author motoryang
 */
@Slf4j
@Tag(name = "通知管理", description = "通知的增删改查、发布与撤销")
@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final INoticeService noticeService;

    /**
     * 分页查询通知
     */
    @Operation(summary = "分页查询通知", description = "根据条件分页查询通知列表")
    @GetMapping
    public RestResult<IPage<NoticeVO>> page(NoticeQueryDTO dto) {
        return RestResult.ok(noticeService.pageQuery(dto));
    }

    /**
     * 获取通知详情
     */
    @Operation(summary = "获取通知详情", description = "根据通知ID获取通知详细信息")
    @GetMapping("/{id}")
    public RestResult<NoticeDetailVO> getDetail(
            @Parameter(description = "通知ID", required = true) @PathVariable String id) {
        return RestResult.ok(noticeService.getNoticeDetail(id));
    }

    /**
     * 创建通知
     */
    @Operation(summary = "创建通知", description = "新增一条通知（草稿状态）")
    @PostMapping
    public RestResult<NoticeVO> create(@Valid @RequestBody NoticeCreateDTO dto) {
        return RestResult.ok(noticeService.createNotice(dto));
    }

    /**
     * 更新通知
     */
    @Operation(summary = "更新通知", description = "根据通知ID更新通知信息")
    @PutMapping("/{id}")
    public RestResult<NoticeVO> update(
            @Parameter(description = "通知ID", required = true) @PathVariable String id,
            @Valid @RequestBody NoticeUpdateDTO dto) {
        return RestResult.ok(noticeService.updateNotice(id, dto));
    }

    /**
     * 删除通知
     */
    @Operation(summary = "删除通知", description = "根据通知ID删除通知（逻辑删除）")
    @DeleteMapping("/{id}")
    public RestResult<Void> delete(
            @Parameter(description = "通知ID", required = true) @PathVariable String id) {
        noticeService.deleteNotice(id);
        return RestResult.ok();
    }

    /**
     * 发布通知
     */
    @Operation(summary = "发布通知", description = "将草稿状态的通知发布")
    @PutMapping("/{id}/publish")
    public RestResult<Void> publish(
            @Parameter(description = "通知ID", required = true) @PathVariable String id) {
        noticeService.publishNotice(id);
        return RestResult.ok();
    }

    /**
     * 撤销通知
     */
    @Operation(summary = "撤销通知", description = "撤销已发布的通知")
    @PutMapping("/{id}/revoke")
    public RestResult<Void> revoke(
            @Parameter(description = "通知ID", required = true) @PathVariable String id) {
        noticeService.revokeNotice(id);
        return RestResult.ok();
    }
}
