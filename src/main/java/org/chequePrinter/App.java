package org.chequePrinter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.chequePrinter.service.DatabaseService;
import org.chequePrinter.util.ExceptionHandler;
import org.chequePrinter.util.LogCleanupService;
import org.chequePrinter.util.LoggerUtil;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

public class App extends Application {
    
    private static final Logger logger = LoggerUtil.getLogger(App.class);

    @Override
    public void start(Stage stage) throws IOException {
        // Setup global exception handler
        ExceptionHandler.setupGlobalExceptionHandler();
        
        // Create logs directory if it doesn't exist
        createLogsDirectory();
        
        // Perform automatic log cleanup (delete logs older than 7 days)
        LogCleanupService.performStartupCleanup();
        
        // Log application startup
        logger.info("=== Cheque Printer Application Starting ===");
        logger.info("Java Version: {}", System.getProperty("java.version"));
        logger.info("JavaFX Version: {}", System.getProperty("javafx.version"));
        logger.info("Operating System: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
        
        // Log current logs directory status
        LogCleanupService.LogsInfo logsInfo = LogCleanupService.getLogsInfo();
        logger.info("Logs directory status: {}", logsInfo.toString());
        
        try {
            // Initialize database
            LoggerUtil.logOperationStart(logger, "database_initialization");
            DatabaseService.initializeDatabase();
            LoggerUtil.logOperationSuccess(logger, "database_initialization");
            
            // Load main UI
            LoggerUtil.logOperationStart(logger, "ui_initialization");
            Parent root = FXMLLoader.load(getClass().getResource("/org/chequePrinter/view/ChequeView.fxml"));
            Scene scene = new Scene(root);
            
            // Setup stage
            stage.setScene(scene);
            stage.setTitle("Cheque Printer - Production Ready");
            stage.setWidth(1024);
            stage.setHeight(768);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.setResizable(true);
            
            // Handle window close event
            stage.setOnCloseRequest(event -> {
                logger.info("Application shutdown requested by user");
                logger.info("=== Cheque Printer Application Shutting Down ===");
            });
            
            stage.show();
            LoggerUtil.logOperationSuccess(logger, "ui_initialization");
            
            logger.info("Application started successfully");
            
        } catch (Exception e) {
            ExceptionHandler.handleException(logger, "application_startup", e,
                "Failed to start the application. Please check the logs for details.");
            throw e;
        }
    }
    
    private void createLogsDirectory() {
        try {
            File logsDir = new File("logs");
            if (!logsDir.exists()) {
                boolean created = logsDir.mkdirs();
                if (created) {
                    logger.info("Created logs directory: {}", logsDir.getAbsolutePath());
                } else {
                    logger.warn("Failed to create logs directory: {}", logsDir.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            logger.error("Error creating logs directory", e);
        }
    }

    public static void main(String[] args) {
        // Launch the application
        launch(args);
    }
}
