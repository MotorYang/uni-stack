package com.github.motoryang.common.logger.vo;

import java.time.LocalDateTime;

/**
 * Log file information VO
 *
 * @param fileName     file name (without path)
 * @param size         file size in bytes
 * @param lastModified last modified time
 */
public record LogFileVO(
        String fileName,
        long size,
        LocalDateTime lastModified
) {
}
