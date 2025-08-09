package org.chequePrinter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.function.Supplier;

/**
 * Utility class for consistent logging across the application
 */
public class LoggerUtil {
    
    /**
     * Get logger for a specific class
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    /**
     * Log method entry with parameters
     */
    public static void logMethodEntry(Logger logger, String methodName, Object... params) {
        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Entering method: ").append(methodName);
            if (params.length > 0) {
                sb.append(" with parameters: ");
                for (int i = 0; i < params.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(params[i]);
                }
            }
            logger.debug(sb.toString());
        }
    }
    
    /**
     * Log method exit
     */
    public static void logMethodExit(Logger logger, String methodName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Exiting method: {}", methodName);
        }
    }
    
    /**
     * Log method exit with return value
     */
    public static void logMethodExit(Logger logger, String methodName, Object returnValue) {
        if (logger.isDebugEnabled()) {
            logger.debug("Exiting method: {} with return value: {}", methodName, returnValue);
        }
    }
    
    /**
     * Log exception with context
     */
    public static void logException(Logger logger, String context, Exception e) {
        logger.error("Exception in {}: {}", context, e.getMessage(), e);
    }
    
    /**
     * Log exception with context and additional message
     */
    public static void logException(Logger logger, String context, String message, Exception e) {
        logger.error("Exception in {}: {} - {}", context, message, e.getMessage(), e);
    }
    
    /**
     * Log business operation start
     */
    public static void logOperationStart(Logger logger, String operation, Object... params) {
        MDC.put("operation", operation);
        logger.info("Starting operation: {} with parameters: {}", operation, params);
    }
    
    /**
     * Log business operation success
     */
    public static void logOperationSuccess(Logger logger, String operation) {
        logger.info("Operation completed successfully: {}", operation);
        MDC.remove("operation");
    }
    
    /**
     * Log business operation failure
     */
    public static void logOperationFailure(Logger logger, String operation, Exception e) {
        logger.error("Operation failed: {} - {}", operation, e.getMessage(), e);
        MDC.remove("operation");
    }
    
    /**
     * Log user action
     */
    public static void logUserAction(Logger logger, String action, String details) {
        logger.info("User action: {} - {}", action, details);
    }
    
    /**
     * Log database operation
     */
    public static void logDatabaseOperation(Logger logger, String operation, String table, Object... params) {
        logger.debug("Database operation: {} on table: {} with params: {}", operation, table, params);
    }
    
    /**
     * Log performance metric
     */
    public static void logPerformance(Logger logger, String operation, long durationMs) {
        if (durationMs > 1000) {
            logger.warn("Slow operation detected: {} took {}ms", operation, durationMs);
        } else {
            logger.debug("Operation performance: {} took {}ms", operation, durationMs);
        }
    }
    
    /**
     * Execute operation with automatic logging and exception handling
     */
    public static <T> T executeWithLogging(Logger logger, String operationName, Supplier<T> operation) {
        long startTime = System.currentTimeMillis();
        logOperationStart(logger, operationName);
        
        try {
            T result = operation.get();
            long duration = System.currentTimeMillis() - startTime;
            logPerformance(logger, operationName, duration);
            logOperationSuccess(logger, operationName);
            return result;
        } catch (Exception e) {
            logOperationFailure(logger, operationName, e);
            throw new RuntimeException("Operation failed: " + operationName, e);
        }
    }
    
    /**
     * Execute operation with automatic logging (void return)
     */
    public static void executeWithLogging(Logger logger, String operationName, Runnable operation) {
        executeWithLogging(logger, operationName, () -> {
            operation.run();
            return null;
        });
    }
}