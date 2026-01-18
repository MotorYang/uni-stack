package com.github.motoryang.storage.template;

import com.github.motoryang.common.core.enums.MultipartFileType;
import com.github.motoryang.common.core.model.UploadResult;
import com.github.motoryang.storage.properties.MinioProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioTemplate {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    /**
     * 上传文件并返回完整 URL,自动按天分目录,UUID 混淆文件名
     * @param file 文件
     */
    public UploadResult upload(MultipartFile file, MultipartFileType fileType) throws Exception {
        // 1. 校验文件
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        // 2. 构造路径
        var typeFolder = fileType.getType();
        var datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        var datePath = "%s/%s".formatted(typeFolder, datePart);
        var originalName = file.getOriginalFilename();
        var extension = originalName != null ? originalName.substring(originalName.lastIndexOf(".")) : "";
        var fileName = "%s/%s%s".formatted(datePath, UUID.randomUUID().toString().replace("-", ""), extension);

        try (var is = file.getInputStream()) {
            // 3. 执行上传
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.bucketName())
                            .object(fileName)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 4. 拼接访问地址
            String accessUrl;
            if (fileType.isPublic()) {
                accessUrl = "%s/%s/%s".formatted(properties.publicDomain(), properties.bucketName(), fileName);
            } else {
                var minioUrl = this.getPresignedUrl(fileName, 2 * 60 * 60);
                accessUrl = minioUrl.replace(properties.endpoint(), properties.publicDomain());
            }
            return new UploadResult(fileName, accessUrl, file.getSize(), file.getContentType());
        } catch (Exception e) {
            log.error("MinIO 上传失败, 文件名: {}, 异常: {}", originalName, e.getMessage());
            throw new RuntimeException("上传服务不可用");
        }
    }

    /**
     * 生成带签名的临时访问链接
     */
    public String getPresignedUrl(String objectName, int expires) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(properties.bucketName())
                            .object(objectName)
                            .expiry(expires)
                            .build()
            );
        } catch (Exception e) {
            log.error("生成 MinIO 签名失败", e);
            throw new RuntimeException();
        }
    }

}
