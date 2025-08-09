package org.chequePrinter.controller;

import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import org.chequePrinter.util.InMemoryLogAppender;
import org.chequePrinter.util.LoggerUtil;
import org.slf4j.Logger;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for the log viewer window
 */
public class LogViewerController implements Initializable {
    
    private static final Logger logger = LoggerUtil.getLogger(LogViewerController.class);
    
    @FXML private TableView<InMemoryLogAppender.LogEntry> logTable;
    @FXML private TableColumn<InMemoryLogAppender.LogEntry, String> timestampColumn;
    @FXML private TableColumn<InMemoryLogAppender.LogEntry, String> levelColumn;
    @FXML private TableColumn<InMemoryLogAppender.LogEntry, String> loggerColumn;
    @FXML private TableColumn<InMemoryLogAppender.LogEntry, String> messageColumn;
    
    @FXML private ComboBox<String> levelFilter;
    @FXML private TextField searchField;
    @FXML private Button clearLogsButton;
    @FXML private Button copySelectedButton;
    @FXML private Button copyAllButton;
    @FXML private Button refreshButton;
    @FXML private Label logCountLabel;
    
    @FXML private TextArea detailsArea;
    
    private FilteredList<InMemoryLogAppender.LogEntry> filteredLogs;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LoggerUtil.logMethodEntry(logger, "initialize");
        
        try {
            setupTableColumns();
            setupFilters();
            setupEventHandlers();
            refreshLogs();
            
            LoggerUtil.logMethodExit(logger, "initialize");
        } catch (Exception e) {
            LoggerUtil.logException(logger, "initialize", e);
        }
    }
    
    private void setupTableColumns() {
        // Setup table columns
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("formattedTimestamp"));
        timestampColumn.setPrefWidth(150);
        
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        levelColumn.setPrefWidth(80);
        levelColumn.setCellFactory(column -> new TableCell<InMemoryLogAppender.LogEntry, String>() {
            @Override
            protected void updateItem(String level, boolean empty) {
                super.updateItem(level, empty);
                if (empty || level == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(level);
                    // Color code log levels
                    switch (level) {
                        case "ERROR":
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        case "WARN":
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            break;
                        case "INFO":
                            setStyle("-fx-text-fill: blue;");
                            break;
                        case "DEBUG":
                            setStyle("-fx-text-fill: gray;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
        
        loggerColumn.setCellValueFactory(new PropertyValueFactory<>("shortLogger"));
        loggerColumn.setPrefWidth(120);
        
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        messageColumn.setPrefWidth(400);
        
        // Make table sortable by timestamp (newest first)
        timestampColumn.setSortType(TableColumn.SortType.DESCENDING);
        logTable.getSortOrder().add(timestampColumn);
    }
    
    private void setupFilters() {
        // Setup level filter
        levelFilter.getItems().addAll("ALL", "ERROR", "WARN", "INFO", "DEBUG");
        levelFilter.setValue("ALL");
        
        // Setup filtered list
        filteredLogs = new FilteredList<>(InMemoryLogAppender.getLogEntries());
        logTable.setItems(filteredLogs);
        
        // Apply filters when changed
        levelFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }
    
    private void setupEventHandlers() {
        // Selection handler to show details
        logTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showLogDetails(newSelection);
            } else {
                detailsArea.clear();
            }
        });
        
        // Double-click to copy message
        logTable.setRowFactory(tv -> {
            TableRow<InMemoryLogAppender.LogEntry> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    copyLogEntry(row.getItem());
                }
            });
            return row;
        });
    }
    
    private void applyFilters() {
        String levelFilterValue = levelFilter.getValue();
        String searchText = searchField.getText().toLowerCase();
        
        filteredLogs.setPredicate(logEntry -> {
            // Level filter
            if (!"ALL".equals(levelFilterValue) && !levelFilterValue.equals(logEntry.getLevel())) {
                return false;
            }
            
            // Search filter
            if (searchText != null && !searchText.isEmpty()) {
                return logEntry.getMessage().toLowerCase().contains(searchText) ||
                       logEntry.getLogger().toLowerCase().contains(searchText);
            }
            
            return true;
        });
        
        updateLogCount();
    }
    
    private void showLogDetails(InMemoryLogAppender.LogEntry logEntry) {
        StringBuilder details = new StringBuilder();
        details.append("Timestamp: ").append(logEntry.getFormattedTimestamp()).append("\n");
        details.append("Level: ").append(logEntry.getLevel()).append("\n");
        details.append("Logger: ").append(logEntry.getLogger()).append("\n");
        details.append("Message: ").append(logEntry.getMessage()).append("\n");
        
        if (logEntry.getException() != null) {
            details.append("Exception: ").append(logEntry.getException()).append("\n");
        }
        
        detailsArea.setText(details.toString());
    }
    
    private void updateLogCount() {
        int totalLogs = InMemoryLogAppender.getLogEntries().size();
        int filteredCount = filteredLogs.size();
        
        if (totalLogs == filteredCount) {
            logCountLabel.setText(String.format("Showing %d logs", totalLogs));
        } else {
            logCountLabel.setText(String.format("Showing %d of %d logs", filteredCount, totalLogs));
        }
    }
    
    @FXML
    private void refreshLogs() {
        LoggerUtil.logUserAction(logger, "refresh_logs", "User refreshed log viewer");
        
        // The observable list is automatically updated, just need to refresh the table
        logTable.refresh();
        updateLogCount();
        
        // Sort by timestamp (newest first)
        logTable.sort();
    }
    
    @FXML
    private void clearLogs() {
        LoggerUtil.logUserAction(logger, "clear_logs", "User cleared all logs");
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear Logs");
        confirm.setHeaderText("Clear All Logs");
        confirm.setContentText("Are you sure you want to clear all logs? This action cannot be undone.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            InMemoryLogAppender.clearLogs();
            detailsArea.clear();
            updateLogCount();
            
            // Log the clear action (this will be one of the first new entries)
            logger.info("Logs cleared by user");
        }
    }
    
    @FXML
    private void copySelected() {
        InMemoryLogAppender.LogEntry selected = logTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            copyLogEntry(selected);
        } else {
            showAlert("No Selection", "Please select a log entry to copy.");
        }
    }
    
    @FXML
    private void copyAll() {
        LoggerUtil.logUserAction(logger, "copy_all_logs", "User copied all visible logs");
        
        StringBuilder allLogs = new StringBuilder();
        for (InMemoryLogAppender.LogEntry entry : filteredLogs) {
            allLogs.append(formatLogEntryForCopy(entry)).append("\n");
        }
        
        copyToClipboard(allLogs.toString());
        showAlert("Copied", String.format("Copied %d log entries to clipboard.", filteredLogs.size()));
    }
    
    private void copyLogEntry(InMemoryLogAppender.LogEntry logEntry) {
        LoggerUtil.logUserAction(logger, "copy_log_entry", "User copied log entry: " + logEntry.getMessage());
        
        String logText = formatLogEntryForCopy(logEntry);
        copyToClipboard(logText);
        showAlert("Copied", "Log entry copied to clipboard.");
    }
    
    private String formatLogEntryForCopy(InMemoryLogAppender.LogEntry logEntry) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(logEntry.getFormattedTimestamp()).append("] ");
        sb.append(logEntry.getLevel()).append(" ");
        sb.append(logEntry.getShortLogger()).append(" - ");
        sb.append(logEntry.getMessage());
        
        if (logEntry.getException() != null) {
            sb.append(" | Exception: ").append(logEntry.getException());
        }
        
        return sb.toString();
    }
    
    private void copyToClipboard(String text) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}