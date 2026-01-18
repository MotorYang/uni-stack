package com.github.motoryang.storage.config;

import com.github.motoryang.storage.properties.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioAutoConfiguration {

    @Bean
    public MinioClient minioClient(MinioProperties properties) {
        var client = MinioClient.builder()
                .endpoint(properties.endpoint())
                .credentials(properties.accessKey(), properties.secretKey())
                .build();
        // 启动时自动检查并创建Bucket
        try {
            boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(properties.bucketName()).build());
            if (!found) {
                client.makeBucket(MakeBucketArgs.builder().bucket(properties.bucketName()).build());
                log.info("MinIO 自动创建存储桶: [{}]", properties.bucketName());
            }
        } catch (Exception e) {
            log.error("初始化 MinIO 桶失败", e);
        }
        log.info("MinIO 桶{}加载", properties.bucketName());
        return client;
    }

}
