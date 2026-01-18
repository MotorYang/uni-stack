package com.github.motoryang.common.core.model;

/**
 * 文件上传返回结果
 */
public record UploadResult(
        String fileName,   // 存储在 MinIO 中的唯一文件名
        String url,        // 可访问的完整 URL
        long size,         // 文件大小 (bytes)
        String contentType // 文件类型
) {
}
