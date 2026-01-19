package com.github.motoryang.storage.controller;

import com.github.motoryang.common.core.enums.MultipartFileType;
import com.github.motoryang.common.core.model.UploadResult;
import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.common.core.result.ResultCode;
import com.github.motoryang.storage.template.MinioTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储控制器
 *
 * @author motoryang
 */
@Slf4j
@Tag(name = "文件存储", description = "文件上传、下载等存储操作")
@RestController
@RequiredArgsConstructor
public class StorageController {

    private final MinioTemplate minioTemplate;

    /**
     * 通用文件上传接口
     * 开启虚拟线程后，此处的高并发 I/O 读写将不会阻塞平台线程
     *
     * @param file 上传的文件
     * @param type 文件类型（image-图片、video-视频、audio-音频、document-文档、other-其他）
     * @return 上传结果，包含文件访问URL等信息
     */
    @Operation(summary = "文件上传", description = "通用文件上传接口，支持图片、视频、音频、文档等多种类型")
    @PostMapping("/upload")
    public RestResult<UploadResult> upload(
            @Parameter(description = "上传的文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件类型：image-图片、video-视频、audio-音频、document-文档、other-其他", required = true)
            @RequestParam("type") String type) {
        log.info("收到文件上传请求: {}, 大小: {} bytes, 类型: {}", file.getOriginalFilename(), file.getSize(), type);

        if (file.isEmpty()) {
            return RestResult.fail("上传文件不能为空");
        }

        MultipartFileType fileType = MultipartFileType.getByType(type);
        if (fileType == null) {
            return RestResult.fail(ResultCode.BAD_REQUEST.getCode(), "请确认是否传输了正确的文件类型");
        }
        try {
            // 调用我们之前封装好的优雅的 MinioTemplate
            UploadResult result = minioTemplate.upload(file, fileType);
            return RestResult.ok(result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return RestResult.fail(ResultCode.FILE_UPLOAD_ERROR.getCode(), "服务器内部错误，文件上传失败");
        }
    }

}
