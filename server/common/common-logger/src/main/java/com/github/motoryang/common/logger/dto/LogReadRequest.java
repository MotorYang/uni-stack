package com.github.motoryang.common.logger.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for reading log file content
 *
 * @param fileName    file name to read (no path, only filename)
 * @param seekPointer byte offset to start reading from
 * @param pageSize    number of bytes to read (default 50KB, max 1MB)
 */
public record LogReadRequest(
        @NotBlank(message = "fileName is required")
        @Pattern(regexp = "^[\\w\\-\\.]+\\.log$", message = "Invalid file name format")
        String fileName,

        @Min(value = 0, message = "seekPointer must be >= 0")
        Long seekPointer,

        @Min(value = 1, message = "pageSize must be >= 1")
        Integer pageSize
) {
    private static final int DEFAULT_PAGE_SIZE = 50 * 1024; // 50KB
    private static final int MAX_PAGE_SIZE = 1024 * 1024;   // 1MB

    public LogReadRequest {
        if (seekPointer == null) {
            seekPointer = 0L;
        }
        if (pageSize == null) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
    }
}
