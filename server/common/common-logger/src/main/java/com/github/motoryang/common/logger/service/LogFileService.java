package com.github.motoryang.common.logger.service;

import com.github.motoryang.common.logger.dto.LogReadRequest;
import com.github.motoryang.common.logger.vo.LogContentVO;
import com.github.motoryang.common.logger.vo.LogFileVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for reading log files with security protection against path traversal.
 * Uses FileChannel for high-performance random access without loading entire file into memory.
 */
@Service
public class LogFileService {

    private static final Logger log = LoggerFactory.getLogger(LogFileService.class);

    private static final int DEFAULT_TAIL_LINES = 1000;
    private static final int TAIL_BUFFER_SIZE = 8 * 1024; // 8KB buffer for tail reading

    @Value("${spring.application.name:application}")
    private String applicationName;

    @Value("${logging.file.path:./logs}")
    private String logPath;

    /**
     * Pattern for valid log file names: alphanumeric, dash, underscore, dot, ending with .log
     */
    private static final Pattern VALID_FILENAME_PATTERN = Pattern.compile("^[\\w\\-\\.]+\\.log$");

    /**
     * Get the history directory path
     */
    private Path getHistoryPath() {
        return Path.of(logPath, "history").toAbsolutePath().normalize();
    }

    /**
     * Get current logs directory path
     */
    private Path getCurrentLogPath() {
        return Path.of(logPath).toAbsolutePath().normalize();
    }

    /**
     * Validate filename to prevent path traversal attacks.
     * Only allows filenames that:
     * - Match the safe pattern (no path separators, no ..)
     * - Start with the application name (for history files) or exact match for current log
     *
     * @param fileName the filename to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }

        // Check against pattern (no path separators allowed)
        if (!VALID_FILENAME_PATTERN.matcher(fileName).matches()) {
            return false;
        }

        // Additional security: ensure no path traversal sequences
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return false;
        }

        return true;
    }

    /**
     * Resolve and validate file path, ensuring it's within allowed directories.
     *
     * @param fileName the filename to resolve
     * @return resolved Path if valid
     * @throws SecurityException if path traversal is detected
     * @throws IllegalArgumentException if file doesn't exist
     */
    private Path resolveAndValidatePath(String fileName) {
        if (!isValidFileName(fileName)) {
            throw new SecurityException("Invalid file name: " + fileName);
        }

        // Try history directory first
        var historyPath = getHistoryPath();
        var resolvedPath = historyPath.resolve(fileName).toAbsolutePath().normalize();

        // Security check: ensure resolved path is under history directory
        if (resolvedPath.startsWith(historyPath) && Files.exists(resolvedPath)) {
            return resolvedPath;
        }

        // Try current log directory
        var currentPath = getCurrentLogPath();
        resolvedPath = currentPath.resolve(fileName).toAbsolutePath().normalize();

        // Security check: ensure resolved path is under current log directory
        if (resolvedPath.startsWith(currentPath) && Files.exists(resolvedPath)) {
            return resolvedPath;
        }

        throw new IllegalArgumentException("File not found: " + fileName);
    }

    /**
     * List all log files for the current service.
     * Returns files from both current directory and history directory.
     *
     * @return list of log file information
     */
    public List<LogFileVO> listLogFiles() throws IOException {
        var historyPath = getHistoryPath();
        var currentPath = getCurrentLogPath();

        // Create directories if they don't exist
        Files.createDirectories(historyPath);

        var files = new java.util.ArrayList<LogFileVO>();

        // Add current log file if exists
        var currentLogFile = currentPath.resolve(applicationName + ".log");
        if (Files.exists(currentLogFile)) {
            files.add(createLogFileVO(currentLogFile));
        }

        // Add current error log if exists
        var currentErrorFile = currentPath.resolve(applicationName + "-error.log");
        if (Files.exists(currentErrorFile)) {
            files.add(createLogFileVO(currentErrorFile));
        }

        // Add history files (files starting with application name)
        try (var stream = Files.list(historyPath)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().startsWith(applicationName))
                    .filter(p -> p.getFileName().toString().endsWith(".log"))
                    .map(this::createLogFileVO)
                    .forEach(files::add);
        }

        // Sort by last modified time (newest first)
        files.sort((a, b) -> b.lastModified().compareTo(a.lastModified()));

        return files;
    }

    private LogFileVO createLogFileVO(Path path) {
        try {
            var attrs = Files.readAttributes(path, BasicFileAttributes.class);
            var lastModified = LocalDateTime.ofInstant(
                    attrs.lastModifiedTime().toInstant(),
                    ZoneId.systemDefault()
            );
            return new LogFileVO(
                    path.getFileName().toString(),
                    attrs.size(),
                    lastModified
            );
        } catch (IOException e) {
            log.warn("Failed to read file attributes: {}", path, e);
            return new LogFileVO(path.getFileName().toString(), 0L, LocalDateTime.now());
        }
    }

    /**
     * Read log content from specified position using FileChannel for high performance.
     *
     * @param request read request containing fileName, seekPointer, and pageSize
     * @return log content with next pointer and total size
     */
    public LogContentVO readLogContent(LogReadRequest request) throws IOException {
        var filePath = resolveAndValidatePath(request.fileName());

        try (var raf = new RandomAccessFile(filePath.toFile(), "r");
             var channel = raf.getChannel()) {

            var totalSize = channel.size();
            var seekPointer = Math.min(request.seekPointer(), totalSize);
            var pageSize = request.pageSize();

            // Calculate actual bytes to read
            var bytesToRead = (int) Math.min(pageSize, totalSize - seekPointer);

            if (bytesToRead <= 0) {
                return new LogContentVO("", totalSize, totalSize, request.fileName());
            }

            // Position channel and read
            channel.position(seekPointer);
            var buffer = ByteBuffer.allocate(bytesToRead);
            var bytesRead = channel.read(buffer);

            if (bytesRead <= 0) {
                return new LogContentVO("", totalSize, totalSize, request.fileName());
            }

            buffer.flip();
            var content = StandardCharsets.UTF_8.decode(buffer).toString();
            var nextPointer = seekPointer + bytesRead;

            return new LogContentVO(content, nextPointer, totalSize, request.fileName());
        }
    }

    /**
     * Read the last N lines of a log file (tail functionality).
     * Uses reverse reading strategy for efficiency on large files.
     *
     * @param fileName file name to read
     * @param lines    number of lines to read (default 1000)
     * @return log content from the end of file
     */
    public LogContentVO tailLogContent(String fileName, Integer lines) throws IOException {
        var filePath = resolveAndValidatePath(fileName);
        var lineCount = (lines == null || lines <= 0) ? DEFAULT_TAIL_LINES : Math.min(lines, 10000);

        try (var raf = new RandomAccessFile(filePath.toFile(), "r");
             var channel = raf.getChannel()) {

            var totalSize = channel.size();

            if (totalSize == 0) {
                return new LogContentVO("", 0L, 0L, fileName);
            }

            // Read backwards to find line breaks
            var linesFound = 0;
            var position = totalSize;
            var buffer = ByteBuffer.allocate(TAIL_BUFFER_SIZE);

            // Search backwards for newlines
            while (position > 0 && linesFound <= lineCount) {
                var bytesToRead = (int) Math.min(TAIL_BUFFER_SIZE, position);
                position -= bytesToRead;

                channel.position(position);
                buffer.clear();
                buffer.limit(bytesToRead);
                channel.read(buffer);
                buffer.flip();

                // Count newlines in this buffer (backwards)
                for (var i = buffer.limit() - 1; i >= 0; i--) {
                    if (buffer.get(i) == '\n') {
                        linesFound++;
                        if (linesFound > lineCount) {
                            // Found enough lines, adjust position to start after this newline
                            position += i + 1;
                            break;
                        }
                    }
                }
            }

            // Read from calculated position to end
            var bytesToRead = (int) (totalSize - position);
            channel.position(position);

            var contentBuffer = ByteBuffer.allocate(bytesToRead);
            channel.read(contentBuffer);
            contentBuffer.flip();

            var content = StandardCharsets.UTF_8.decode(contentBuffer).toString();

            // Trim leading newline if position was adjusted
            if (!content.isEmpty() && content.charAt(0) == '\n') {
                content = content.substring(1);
            }

            return new LogContentVO(content, totalSize, totalSize, fileName);
        }
    }

    /**
     * Download log file by streaming to output.
     * Uses FileChannel for efficient transfer without loading entire file into memory.
     *
     * @param fileName     file name to download
     * @param outputStream output stream to write file content
     */
    public void downloadLogFile(String fileName, OutputStream outputStream) throws IOException {
        var filePath = resolveAndValidatePath(fileName);

        try (var channel = FileChannel.open(filePath);
             var outChannel = Channels.newChannel(outputStream)) {

            var size = channel.size();
            var position = 0L;

            // Transfer in chunks for memory efficiency
            while (position < size) {
                var transferred = channel.transferTo(position, size - position, outChannel);
                if (transferred <= 0) {
                    break;
                }
                position += transferred;
            }
        }
    }
}
