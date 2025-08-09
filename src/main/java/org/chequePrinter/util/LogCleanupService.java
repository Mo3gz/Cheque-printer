package org.chequePrinter.util;

import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for automatically cleaning up old log files to keep the application optimized
 */
public class LogCleanupService {
    
    private static final Logger logger = LoggerUtil.getLogger(LogCleanupService.class);
    private static final String LOGS_DIRECTORY = "logs";
    private static final int RETENTION_DAYS = 7;
    
    /**
     * Performs automatic cleanup of log files older than 7 days
     * This method is called during application startup
     */
    public static void performStartupCleanup() {
        LoggerUtil.logMethodEntry(logger, "performStartupCleanup");
        
        try {
            File logsDir = new File(LOGS_DIRECTORY);
            
            if (!logsDir.exists()) {
                logger.debug("Logs directory does not exist, no cleanup needed");
                return;
            }
            
            if (!logsDir.isDirectory()) {
                logger.warn("Logs path exists but is not a directory: {}", logsDir.getAbsolutePath());
                return;
            }
            
            LoggerUtil.logOperationStart(logger, "log_cleanup", RETENTION_DAYS + " days retention");
            
            List<File> deletedFiles = cleanupOldLogFiles(logsDir);
            
            if (deletedFiles.isEmpty()) {
                logger.info("Log cleanup completed - no old files found to delete");
            } else {
                logger.info("Log cleanup completed - deleted {} old log files", deletedFiles.size());
                for (File deletedFile : deletedFiles) {
                    logger.debug("Deleted old log file: {}", deletedFile.getName());
                }
            }
            
            // Log current disk usage
            logDiskUsage(logsDir);
            
            LoggerUtil.logOperationSuccess(logger, "log_cleanup");
            
        } catch (Exception e) {
            LoggerUtil.logException(logger, "performStartupCleanup", e);
        }
        
        LoggerUtil.logMethodExit(logger, "performStartupCleanup");
    }
    
    /**
     * Cleans up log files older than the retention period
     */
    private static List<File> cleanupOldLogFiles(File logsDirectory) {
        List<File> deletedFiles = new ArrayList<>();
        Instant cutoffTime = Instant.now().minus(RETENTION_DAYS, ChronoUnit.DAYS);
        
        logger.debug("Cleaning up log files older than {} days (before {})", RETENTION_DAYS, cutoffTime);
        
        File[] logFiles = logsDirectory.listFiles();
        if (logFiles == null) {
            logger.warn("Could not list files in logs directory");
            return deletedFiles;
        }
        
        for (File file : logFiles) {
            try {
                if (file.isFile() && isLogFile(file) && isOlderThanCutoff(file, cutoffTime)) {
                    if (file.delete()) {
                        deletedFiles.add(file);
                        logger.debug("Successfully deleted old log file: {}", file.getName());
                    } else {
                        logger.warn("Failed to delete old log file: {}", file.getName());
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing log file {}: {}", file.getName(), e.getMessage());
            }
        }
        
        return deletedFiles;
    }
    
    /**
     * Checks if a file is a log file based on its name and extension
     */
    private static boolean isLogFile(File file) {
        String fileName = file.getName().toLowerCase();
        
        // Match various log file patterns
        return fileName.endsWith(".log") || 
               fileName.contains("cheque-printer") ||
               fileName.matches(".*\\.\\d{4}-\\d{2}-\\d{2}\\.\\d+\\.log") || // Rotated logs
               fileName.matches(".*\\.\\d{4}-\\d{2}-\\d{2}\\.log"); // Date-based logs
    }
    
    /**
     * Checks if a file is older than the cutoff time
     */
    private static boolean isOlderThanCutoff(File file, Instant cutoffTime) {
        try {
            Path filePath = Paths.get(file.getAbsolutePath());
            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
            
            // Use the creation time or last modified time, whichever is older
            Instant fileTime = attrs.creationTime().toInstant();
            Instant modifiedTime = attrs.lastModifiedTime().toInstant();
            
            // Use the more recent of creation or modification time
            Instant actualFileTime = fileTime.isAfter(modifiedTime) ? fileTime : modifiedTime;
            
            boolean isOld = actualFileTime.isBefore(cutoffTime);
            
            if (isOld) {
                logger.debug("File {} is old (created/modified: {})", file.getName(), actualFileTime);
            }
            
            return isOld;
            
        } catch (IOException e) {
            logger.error("Could not read file attributes for {}: {}", file.getName(), e.getMessage());
            return false; // Don't delete if we can't determine age
        }
    }
    
    /**
     * Logs current disk usage of the logs directory
     */
    private static void logDiskUsage(File logsDirectory) {
        try {
            long totalSize = 0;
            int fileCount = 0;
            
            File[] files = logsDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        totalSize += file.length();
                        fileCount++;
                    }
                }
            }
            
            String sizeFormatted = formatFileSize(totalSize);
            logger.info("Current logs directory usage: {} files, {} total size", fileCount, sizeFormatted);
            
        } catch (Exception e) {
            logger.debug("Could not calculate logs directory usage: {}", e.getMessage());
        }
    }
    
    /**
     * Formats file size in human-readable format
     */
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Manually trigger log cleanup (can be called from UI if needed)
     */
    public static void manualCleanup() {
        logger.info("Manual log cleanup triggered by user");
        performStartupCleanup();
    }
    
    /**
     * Get information about the logs directory
     */
    public static LogsInfo getLogsInfo() {
        try {
            File logsDir = new File(LOGS_DIRECTORY);
            if (!logsDir.exists() || !logsDir.isDirectory()) {
                return new LogsInfo(0, 0, "Logs directory not found");
            }
            
            File[] files = logsDir.listFiles();
            if (files == null) {
                return new LogsInfo(0, 0, "Could not read logs directory");
            }
            
            long totalSize = 0;
            int fileCount = 0;
            
            for (File file : files) {
                if (file.isFile()) {
                    totalSize += file.length();
                    fileCount++;
                }
            }
            
            return new LogsInfo(fileCount, totalSize, formatFileSize(totalSize));
            
        } catch (Exception e) {
            return new LogsInfo(0, 0, "Error reading logs info: " + e.getMessage());
        }
    }
    
    /**
     * Information about the logs directory
     */
    public static class LogsInfo {
        private final int fileCount;
        private final long totalSize;
        private final String formattedSize;
        
        public LogsInfo(int fileCount, long totalSize, String formattedSize) {
            this.fileCount = fileCount;
            this.totalSize = totalSize;
            this.formattedSize = formattedSize;
        }
        
        public int getFileCount() { return fileCount; }
        public long getTotalSize() { return totalSize; }
        public String getFormattedSize() { return formattedSize; }
        
        @Override
        public String toString() {
            return String.format("%d files, %s", fileCount, formattedSize);
        }
    }
}