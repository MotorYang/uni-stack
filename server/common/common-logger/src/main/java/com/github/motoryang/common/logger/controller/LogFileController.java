package com.github.motoryang.common.logger.controller;

import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.common.logger.dto.LogReadRequest;
import com.github.motoryang.common.logger.service.LogFileService;
import com.github.motoryang.common.logger.vo.LogContentVO;
import com.github.motoryang.common.logger.vo.LogFileVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Log file viewer controller.
 * Provides endpoints for listing and reading log files with security protection.
 *
 * <p>Security: All file access is validated to prevent path traversal attacks.
 * Only files within the configured log directories can be accessed.</p>
 */
@RestController
@RequestMapping("/logs")
public class LogFileController {

    private static final Logger log = LoggerFactory.getLogger(LogFileController.class);

    private final LogFileService logFileService;

    public LogFileController(LogFileService logFileService) {
        this.logFileService = logFileService;
    }

    /**
     * List all available log files for the current service.
     * Returns files from both current directory (active logs) and history directory (rolled logs).
     *
     * @return list of log files with metadata
     */
    @GetMapping("/files")
    public RestResult<List<LogFileVO>> listFiles() {
        try {
            var files = logFileService.listLogFiles();
            return RestResult.ok(files);
        } catch (IOException e) {
            log.error("Failed to list log files", e);
            return RestResult.fail("Failed to list log files: " + e.getMessage());
        }
    }

    /**
     * Read log content with pagination support.
     * Uses byte-based offset for efficient random access on large files.
     *
     * @param fileName    file name (no path, only the filename)
     * @param seekPointer byte offset to start reading from (default 0)
     * @param pageSize    number of bytes to read (default 50KB, max 1MB)
     * @return log content with next pointer and total size
     */
    @GetMapping("/read")
    public RestResult<LogContentVO> readContent(
            @RequestParam
            @Pattern(regexp = "^[\\w\\-\\.]+\\.log$", message = "Invalid file name format")
            String fileName,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "seekPointer must be >= 0")
            Long seekPointer,

            @RequestParam(defaultValue = "51200")
            @Min(value = 1, message = "pageSize must be >= 1")
            @Max(value = 1048576, message = "pageSize must be <= 1MB")
            Integer pageSize
    ) {
        try {
            var request = new LogReadRequest(fileName, seekPointer, pageSize);
            var content = logFileService.readLogContent(request);
            return RestResult.ok(content);
        } catch (SecurityException e) {
            log.warn("Security violation attempt: {}", e.getMessage());
            return RestResult.fail(403, "Access denied: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return RestResult.fail(404, e.getMessage());
        } catch (IOException e) {
            log.error("Failed to read log file: {}", fileName, e);
            return RestResult.fail("Failed to read log file: " + e.getMessage());
        }
    }

    /**
     * Read the last N lines of a log file (tail functionality).
     * Efficiently reads from the end of file without loading entire content.
     *
     * @param fileName file name (no path, only the filename)
     * @param lines    number of lines to read (default 1000, max 10000)
     * @return log content from the end of file
     */
    @GetMapping("/tail")
    public RestResult<LogContentVO> tailContent(
            @RequestParam
            @Pattern(regexp = "^[\\w\\-\\.]+\\.log$", message = "Invalid file name format")
            String fileName,

            @RequestParam(defaultValue = "1000")
            @Min(value = 1, message = "lines must be >= 1")
            @Max(value = 10000, message = "lines must be <= 10000")
            Integer lines
    ) {
        try {
            var content = logFileService.tailLogContent(fileName, lines);
            return RestResult.ok(content);
        } catch (SecurityException e) {
            log.warn("Security violation attempt: {}", e.getMessage());
            return RestResult.fail(403, "Access denied: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return RestResult.fail(404, e.getMessage());
        } catch (IOException e) {
            log.error("Failed to tail log file: {}", fileName, e);
            return RestResult.fail("Failed to tail log file: " + e.getMessage());
        }
    }

    /**
     * Download a log file.
     * Streams the file content directly to the response for efficient download of large files.
     *
     * @param fileName file name (no path, only the filename)
     * @param response HTTP response for streaming
     */
    @GetMapping("/download")
    public void downloadFile(
            @RequestParam
            @Pattern(regexp = "^[\\w\\-\\.]+\\.log$", message = "Invalid file name format")
            String fileName,
            HttpServletResponse response
    ) {
        try {
            // Set response headers for file download
            var encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

            // Stream file to response
            logFileService.downloadLogFile(fileName, response.getOutputStream());
            response.flushBuffer();
        } catch (SecurityException e) {
            log.warn("Security violation attempt: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IOException e) {
            log.error("Failed to download log file: {}", fileName, e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to download log file: " + e.getMessage());
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) {
        try {
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + message + "\",\"data\":null}");
        } catch (IOException e) {
            log.error("Failed to send error response", e);
        }
    }
}
