package org.chequePrinter.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Centralized exception handling utility for the application
 */
public class ExceptionHandler {
    
    /**
     * Handle exception with user-friendly message and logging
     */
    public static void handleException(Logger logger, String context, Exception e, String userMessage) {
        // Log the exception
        LoggerUtil.logException(logger, context, e);
        
        // Show user-friendly message
        Platform.runLater(() -> showErrorDialog("Error", userMessage, e));
    }
    
    /**
     * Handle exception with default user message
     */
    public static void handleException(Logger logger, String context, Exception e) {
        handleException(logger, context, e, "An unexpected error occurred. Please check the logs for details.");
    }
    
    /**
     * Execute operation with exception handling
     */
    public static <T> Optional<T> executeWithExceptionHandling(Logger logger, String operationName, 
                                                               Supplier<T> operation, String errorMessage) {
        try {
            LoggerUtil.logOperationStart(logger, operationName);
            T result = operation.get();
            LoggerUtil.logOperationSuccess(logger, operationName);
            return Optional.of(result);
        } catch (Exception e) {
            handleException(logger, operationName, e, errorMessage);
            return Optional.empty();
        }
    }
    
    /**
     * Execute void operation with exception handling
     */
    public static boolean executeWithExceptionHandling(Logger logger, String operationName, 
                                                       Runnable operation, String errorMessage) {
        try {
            LoggerUtil.logOperationStart(logger, operationName);
            operation.run();
            LoggerUtil.logOperationSuccess(logger, operationName);
            return true;
        } catch (Exception e) {
            handleException(logger, operationName, e, errorMessage);
            return false;
        }
    }
    
    /**
     * Execute operation with custom exception handler
     */
    public static <T> Optional<T> executeWithCustomHandler(Logger logger, String operationName, 
                                                           Supplier<T> operation, 
                                                           Function<Exception, T> exceptionHandler) {
        try {
            LoggerUtil.logOperationStart(logger, operationName);
            T result = operation.get();
            LoggerUtil.logOperationSuccess(logger, operationName);
            return Optional.of(result);
        } catch (Exception e) {
            LoggerUtil.logException(logger, operationName, e);
            try {
                T fallbackResult = exceptionHandler.apply(e);
                return Optional.ofNullable(fallbackResult);
            } catch (Exception handlerException) {
                LoggerUtil.logException(logger, operationName + " - exception handler", handlerException);
                return Optional.empty();
            }
        }
    }
    
    /**
     * Handle database exceptions specifically
     */
    public static void handleDatabaseException(Logger logger, String operation, Exception e) {
        String userMessage = "Database operation failed. Please ensure the database is accessible and try again.";
        if (e.getMessage().contains("locked")) {
            userMessage = "Database is currently locked. Please wait a moment and try again.";
        } else if (e.getMessage().contains("connection")) {
            userMessage = "Cannot connect to database. Please check if the database file exists and is accessible.";
        }
        handleException(logger, "Database - " + operation, e, userMessage);
    }
    
    /**
     * Handle PDF generation exceptions
     */
    public static void handlePdfException(Logger logger, String operation, Exception e) {
        String userMessage = "PDF generation failed. Please check your template settings and try again.";
        if (e.getMessage().contains("template")) {
            userMessage = "Template file is missing or corrupted. Please select a valid template.";
        } else if (e.getMessage().contains("font")) {
            userMessage = "Font loading failed. Please ensure all required fonts are available.";
        }
        handleException(logger, "PDF - " + operation, e, userMessage);
    }
    
    /**
     * Handle printing exceptions
     */
    public static void handlePrintException(Logger logger, String operation, Exception e) {
        String userMessage = "Printing failed. Please check your printer settings and try again.";
        if (e.getMessage().contains("printer")) {
            userMessage = "No printer found or printer is not available. Please check your printer connection.";
        } else if (e.getMessage().contains("paper")) {
            userMessage = "Paper size or orientation issue. Please check your printer settings.";
        }
        handleException(logger, "Print - " + operation, e, userMessage);
    }
    
    /**
     * Handle validation exceptions
     */
    public static void handleValidationException(Logger logger, String field, String message) {
        logger.warn("Validation failed for field '{}': {}", field, message);
        Platform.runLater(() -> showWarningDialog("Validation Error", message));
    }
    
    /**
     * Show error dialog with exception details
     */
    private static void showErrorDialog(String title, String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText("Error: " + e.getMessage());
        
        // Create expandable Exception details
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();
        
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);
        
        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }
    
    /**
     * Show warning dialog
     */
    private static void showWarningDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show confirmation dialog with exception handling
     */
    public static boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Execute operation with user confirmation
     */
    public static boolean executeWithConfirmation(Logger logger, String operationName, 
                                                  Runnable operation, String confirmationMessage, 
                                                  String errorMessage) {
        if (showConfirmationDialog("Confirm Action", confirmationMessage)) {
            return executeWithExceptionHandling(logger, operationName, operation, errorMessage);
        }
        return false;
    }
    
    /**
     * Global exception handler for uncaught exceptions
     */
    public static void setupGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            Logger logger = LoggerUtil.getLogger(ExceptionHandler.class);
            LoggerUtil.logException(logger, "Uncaught exception in thread: " + thread.getName(), 
                                  new RuntimeException(exception));
            
            Platform.runLater(() -> {
                showErrorDialog("Critical Error", 
                              "A critical error occurred in the application. Please restart the application.", 
                              new RuntimeException(exception));
            });
        });
    }
}