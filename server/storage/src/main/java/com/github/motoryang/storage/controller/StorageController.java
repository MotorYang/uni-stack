package com.github.motoryang.storage.controller;

import com.github.motoryang.common.core.enums.MultipartFileType;
import com.github.motoryang.common.core.model.UploadResult;
import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.common.core.result.ResultCode;
import com.github.motoryang.storage.template.MinioTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StorageController {

    private final MinioTemplate minioTemplate;

    /**
     * 通用文件上传接口
     * 开启虚拟线程后，此处的高并发 I/O 读写将不会阻塞平台线程
     */
    @PostMapping("/upload")
    public RestResult<UploadResult> upload(@RequestParam("file") MultipartFile file, @RequestParam("type") String type) {
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
