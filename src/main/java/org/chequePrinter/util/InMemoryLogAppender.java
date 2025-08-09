package org.chequePrinter.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Custom Logback appender that stores log events in memory for display in the UI
 */
public class InMemoryLogAppender extends AppenderBase<ILoggingEvent> {
    
    private static final int MAX_LOGS = 1000;
    private static final ConcurrentLinkedQueue<LogEntry> logQueue = new ConcurrentLinkedQueue<>();
    private static final ObservableList<LogEntry> observableLogList = FXCollections.observableArrayList();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    @Override
    protected void append(ILoggingEvent event) {
        LogEntry logEntry = new LogEntry(
            LocalDateTime.now(),
            event.getLevel().toString(),
            event.getLoggerName(),
            event.getFormattedMessage(),
            event.getThrowableProxy() != null ? event.getThrowableProxy().getMessage() : null
        );
        
        // Add to queue
        logQueue.offer(logEntry);
        
        // Remove old entries if we exceed max size
        while (logQueue.size() > MAX_LOGS) {
            logQueue.poll();
        }
        
        // Update observable list on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            updateObservableList();
        } else {
            Platform.runLater(this::updateObservableList);
        }
    }
    
    private void updateObservableList() {
        observableLogList.clear();
        observableLogList.addAll(logQueue);
    }
    
    public static ObservableList<LogEntry> getLogEntries() {
        return observableLogList;
    }
    
    public static void clearLogs() {
        logQueue.clear();
        if (Platform.isFxApplicationThread()) {
            observableLogList.clear();
        } else {
            Platform.runLater(() -> observableLogList.clear());
        }
    }
    
    /**
     * Log entry data class
     */
    public static class LogEntry {
        private final LocalDateTime timestamp;
        private final String level;
        private final String logger;
        private final String message;
        private final String exception;
        
        public LogEntry(LocalDateTime timestamp, String level, String logger, String message, String exception) {
            this.timestamp = timestamp;
            this.level = level;
            this.logger = logger;
            this.message = message;
            this.exception = exception;
        }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getLevel() { return level; }
        public String getLogger() { return logger; }
        public String getMessage() { return message; }
        public String getException() { return exception; }
        
        public String getFormattedTimestamp() {
            return timestamp.format(formatter);
        }
        
        public String getShortLogger() {
            String[] parts = logger.split("\\.");
            return parts.length > 0 ? parts[parts.length - 1] : logger;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s %s - %s", 
                timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")), 
                level, 
                getShortLogger(), 
                message);
        }
    }
}