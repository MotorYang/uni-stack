package com.github.motoryang.common.logger.vo;

/**
 * Log content read response VO
 *
 * @param content     text content read from file
 * @param nextPointer byte offset for next read (for pagination)
 * @param totalSize   total file size in bytes
 * @param fileName    file name being read
 */
public record LogContentVO(
        String content,
        long nextPointer,
        long totalSize,
        String fileName
) {
}
