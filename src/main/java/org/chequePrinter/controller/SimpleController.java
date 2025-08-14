package org.chequePrinter.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.chequePrinter.model.BankTemplate;
import org.chequePrinter.model.ChequeData;
import org.chequePrinter.service.PaymentPlanService;
import org.chequePrinter.service.PdfPrinter;
import org.chequePrinter.util.ExceptionHandler;
import org.chequePrinter.util.InMemoryLogAppender;
import org.chequePrinter.util.LogCleanupService;
import org.chequePrinter.util.LoggerUtil;
import org.slf4j.Logger;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SimpleController implements Initializable {

    private static final Logger logger = LoggerUtil.getLogger(SimpleController.class);

    // FXML UI Components
    @FXML private Pane previewPane;
    @FXML private ImageView chequeImageView;
    @FXML private Text dateText;
    @FXML private Text beneficiaryText;
    @FXML private Text amountWordsText;
    @FXML private Text amountText;
    @FXML private Text signerText;

    @FXML private ComboBox<BankTemplate> bankComboBox;
    @FXML private ComboBox<BankTemplate.Template> templateComboBox;
    @FXML private DatePicker firstCheckDatePicker;
    @FXML private TextField beneficiaryField;
    @FXML private TextField amountField;
    @FXML private TextField amountWordsField;
    @FXML private TextField signerField;
    @FXML private TextField numChecksField;
    @FXML private ComboBox<String> intervalComboBox;
    @FXML private TextField phoneNumberField;

    @FXML private TableView<ChequeData> chequeTableView;
    @FXML private TableColumn<ChequeData, Integer> idColumn;
    @FXML private TableColumn<ChequeData, String> dateColumn;
    @FXML private TableColumn<ChequeData, String> beneficiaryColumn;
    @FXML private TableColumn<ChequeData, String> amountNumericColumn;
    @FXML private TableColumn<ChequeData, String> amountWordsColumn;
    @FXML private TableColumn<ChequeData, String> signerColumn;
    @FXML private TableColumn<ChequeData, String> phoneNumberColumn;

    @FXML private TextField filterBeneficiaryField;
    @FXML private TextField filterPhoneNumberField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    // Multiple cheque editing table
    @FXML private TableView<ChequeData> multiChequeTableView;
    @FXML private TableColumn<ChequeData, String> multiDateColumn;
    @FXML private TableColumn<ChequeData, String> multiBeneficiaryColumn;
    @FXML private TableColumn<ChequeData, String> multiAmountColumn;
    @FXML private TableColumn<ChequeData, String> multiAmountWordsColumn;
    @FXML private TableColumn<ChequeData, String> multiSignerColumn;
    @FXML private TableColumn<ChequeData, String> multiPhoneNumberColumn;
    @FXML private TableColumn<ChequeData, Void> multiActionColumn;

    // Log viewer components
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
    @FXML private Button cleanupOldLogsButton;
    @FXML private Label logCountLabel;
    @FXML private Label logsDirectoryInfo;
    @FXML private TextArea detailsArea;

    // Refactored sub-controllers
    private ChequeFormController formController;
    private ChequePreviewController previewController;
    private ChequePrintController printController;
    private ChequeDataController dataController;
    private ChequeFilterController filterController;
    private ChequeExportController exportController;
    private BankTemplateController templateController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LoggerUtil.logMethodEntry(logger, "initialize");
        
        try {
            // Initialize sub-controllers
            initializeSubControllers();
            setupControllerDependencies();
            setupEventHandlers();
            setupLogViewer();
            
            // Log successful initialization
            logger.info("SimpleController initialized successfully");
            LoggerUtil.logMethodExit(logger, "initialize");
        } catch (Exception e) {
            ExceptionHandler.handleException(logger, "initialize", e, "Failed to initialize main controller");
        }
    }

    private void initializeSubControllers() {
        // Create and initialize sub-controllers
        formController = new ChequeFormController();
        previewController = new ChequePreviewController();
        printController = new ChequePrintController();
        dataController = new ChequeDataController();
        filterController = new ChequeFilterController();
        exportController = new ChequeExportController();
        templateController = new BankTemplateController();

        // Inject FXML components into sub-controllers
        injectFXMLComponents();

        // Initialize all sub-controllers
        formController.initialize();
        previewController.initialize();
        dataController.initialize();
        filterController.initialize();
        exportController.initialize();
        templateController.initialize();
    }

    private void injectFXMLComponents() {
        // Inject form components
        setPrivateField(formController, "firstCheckDatePicker", firstCheckDatePicker);
        setPrivateField(formController, "beneficiaryField", beneficiaryField);
        setPrivateField(formController, "amountField", amountField);
        setPrivateField(formController, "amountWordsField", amountWordsField);
        setPrivateField(formController, "signerField", signerField);
        setPrivateField(formController, "numChecksField", numChecksField);
        setPrivateField(formController, "intervalComboBox", intervalComboBox);
        setPrivateField(formController, "phoneNumberField", phoneNumberField);

        // Inject preview components
        setPrivateField(previewController, "previewPane", previewPane);
        setPrivateField(previewController, "chequeImageView", chequeImageView);
        setPrivateField(previewController, "dateText", dateText);
        setPrivateField(previewController, "beneficiaryText", beneficiaryText);
        setPrivateField(previewController, "amountWordsText", amountWordsText);
        setPrivateField(previewController, "amountText", amountText);
        setPrivateField(previewController, "signerText", signerText);

        // Inject template components
        setPrivateField(templateController, "bankComboBox", bankComboBox);
        setPrivateField(templateController, "templateComboBox", templateComboBox);

        // Inject data components
        setPrivateField(dataController, "chequeTableView", chequeTableView);
        setPrivateField(dataController, "idColumn", idColumn);
        setPrivateField(dataController, "dateColumn", dateColumn);
        setPrivateField(dataController, "beneficiaryColumn", beneficiaryColumn);
        setPrivateField(dataController, "amountNumericColumn", amountNumericColumn);
        setPrivateField(dataController, "amountWordsColumn", amountWordsColumn);
        setPrivateField(dataController, "signerColumn", signerColumn);
        setPrivateField(dataController, "phoneNumberColumn", phoneNumberColumn);
        setPrivateField(dataController, "multiChequeTableView", multiChequeTableView);
        setPrivateField(dataController, "multiDateColumn", multiDateColumn);
        setPrivateField(dataController, "multiBeneficiaryColumn", multiBeneficiaryColumn);
        setPrivateField(dataController, "multiAmountColumn", multiAmountColumn);
        setPrivateField(dataController, "multiAmountWordsColumn", multiAmountWordsColumn);
        setPrivateField(dataController, "multiSignerColumn", multiSignerColumn);
        setPrivateField(dataController, "multiPhoneNumberColumn", multiPhoneNumberColumn);
        setPrivateField(dataController, "multiActionColumn", multiActionColumn);

        // Inject filter components
        setPrivateField(filterController, "filterBeneficiaryField", filterBeneficiaryField);
        setPrivateField(filterController, "filterPhoneNumberField", filterPhoneNumberField);
        setPrivateField(filterController, "startDatePicker", startDatePicker);
        setPrivateField(filterController, "endDatePicker", endDatePicker);
    }

    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            System.err.println("Failed to inject field " + fieldName + ": " + e.getMessage());
        }
    }

    private void setupControllerDependencies() {
        // Set up callbacks and dependencies
        dataController.setOnChequeSelected(() -> {
            ChequeData selectedCheque = dataController.getSelectedCheque();
            if (selectedCheque != null) {
                formController.populateForm(selectedCheque);
                updatePreview();
            }
        });

        dataController.setPrintController(printController);
        dataController.setTemplateController(templateController);
        filterController.setDataController(dataController);
        exportController.setDataController(dataController);
        exportController.setFilterController(filterController);
        templateController.setPreviewController(previewController);
    }

    private void setupEventHandlers() {
        // Set up form field listeners to update preview
        formController.getBeneficiaryField().textProperty().addListener((obs, old, val) -> updatePreview());
        formController.getAmountField().textProperty().addListener((obs, old, val) -> updatePreview());
        formController.getAmountWordsField().textProperty().addListener((obs, old, val) -> updatePreview());
        formController.getSignerField().textProperty().addListener((obs, old, val) -> updatePreview());
        formController.getFirstCheckDatePicker().valueProperty().addListener((obs, old, val) -> updatePreview());
        if (formController.getPhoneNumberField() != null) {
            formController.getPhoneNumberField().textProperty().addListener((obs, old, val) -> updatePreview());
        }
    }

    private void updatePreview() {
        String date = formController.getFirstCheckDatePicker().getValue() != null ?
                     formController.getFirstCheckDatePicker().getValue().toString() : "";
        String beneficiary = formController.getBeneficiaryField().getText();
        String amountWords = formController.getAmountWordsField().getText();
        String amount = formController.getAmountField().getText();
        String signer = formController.getSignerField().getText();

        previewController.updatePreviewWithData(date, beneficiary, amountWords, amount, signer);
    }

    @FXML
    private void printAndSaveSingleCheck() {
        LoggerUtil.logUserAction(logger, "print_single_check", "User initiated single check printing");
        
        if (!formController.validateInput()) {
            LoggerUtil.logUserAction(logger, "print_single_check_validation_failed", "Input validation failed");
            return;
        }

        ExceptionHandler.executeWithExceptionHandling(logger, "printAndSaveSingleCheck", () -> {
            ChequeData chequeData = formController.createChequeData();
            BankTemplate.Template selectedTemplate = templateController.getSelectedTemplate();

            if (selectedTemplate == null) {
                ExceptionHandler.handleValidationException(logger, "template_selection", "Please select a valid bank template before printing.");
                return;
            }

            LoggerUtil.logOperationStart(logger, "print_single_check", chequeData.getBeneficiaryName(), chequeData.getAmountNumeric());

            float widthInCm = selectedTemplate.getWidth();
            float heightInCm = selectedTemplate.getHeight();

            try {
                // Generate PDF and print
                PDDocument document = printController.generateChequePDF(chequeData, selectedTemplate);
                boolean printSuccessful = printController.printPDF(document, widthInCm, heightInCm);
                
                if (printSuccessful) {
                    // Only save to database if printing was successful (user didn't cancel)
                    dataController.saveCheque(chequeData);
                    logger.info("Cheque printed and saved successfully for beneficiary: {}", chequeData.getBeneficiaryName());
                } else {
                    logger.info("Print cancelled by user - cheque not saved to database for beneficiary: {}", chequeData.getBeneficiaryName());
                    showAlert("Print Cancelled", "Print job was cancelled. The cheque was not saved to the database.");
                    return; // Exit without showing success message
                }
            } catch (Exception e) {
                throw new RuntimeException("PDF generation or printing failed", e);
            }
            
            LoggerUtil.logOperationSuccess(logger, "print_single_check");
            showAlert("Success", "Cheque printed and saved successfully!");
            
        }, "Failed to print and save cheque. Please check your printer settings and try again.");
    }

    @FXML
    private void printAndSaveMultipleChecks() {
        LoggerUtil.logUserAction(logger, "print_multiple_checks", "User initiated multiple checks printing");
        
        if (!formController.validateInput()) {
            LoggerUtil.logUserAction(logger, "print_multiple_checks_validation_failed", "Input validation failed");
            return;
        }

        try {
            int numChecks = Integer.parseInt(formController.getNumChecksField().getText());
            String interval = formController.getIntervalComboBox().getValue();
            
            LoggerUtil.logOperationStart(logger, "print_multiple_checks", numChecks, interval);
            
            ExceptionHandler.executeWithExceptionHandling(logger, "printAndSaveMultipleChecks", () -> {
                ChequeData baseData = formController.createChequeData();
                BankTemplate.Template selectedTemplate = templateController.getSelectedTemplate();

                if (selectedTemplate == null) {
                    ExceptionHandler.handleValidationException(logger, "template_selection", "Please select a valid bank template before printing.");
                    return;
                }

                try {
                    // Generate cheque data list
                    List<ChequeData> chequesToPrint = printController.generateChequeDataList(baseData, numChecks, interval);

                    // Create and print PDF
                    PDDocument document = printController.generateMultipleChequePDF(chequesToPrint, selectedTemplate);
                    boolean printSuccessful = printController.printPDF(document, selectedTemplate.getWidth(), selectedTemplate.getHeight());

                    if (printSuccessful) {
                        // Only save records to the database if printing was successful
                        dataController.saveCheques(chequesToPrint);
                        logger.info("Multiple cheques printed and saved successfully: {} cheques", chequesToPrint.size());
                    } else {
                        logger.info("Print cancelled by user - {} cheques not saved to database", chequesToPrint.size());
                        showAlert("Print Cancelled", "Print job was cancelled. The " + chequesToPrint.size() + " cheques were not saved to the database.");
                        return; // Exit without showing success message
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Multiple cheques generation or printing failed", e);
                }
                
                LoggerUtil.logOperationSuccess(logger, "print_multiple_checks");
                showAlert("Success", numChecks + " cheques printed and saved successfully!");
                
            }, "Failed to print multiple cheques. Please check your settings and try again.");
            
        } catch (NumberFormatException e) {
            ExceptionHandler.handleValidationException(logger, "number_of_checks", "Please enter a valid number of checks.");
        }
    }

    @FXML
    private void deleteSelectedRecord() {
        dataController.deleteSelectedRecord();
    }

    @FXML
    private void applyDateFilter() {
        filterController.applyDateFilter();
    }

    @FXML
    public void clearFilters() {
        filterController.clearFilters();
    }

    @FXML
    private void generateMultipleChecks() {
        LoggerUtil.logUserAction(logger, "generate_multiple_checks", "User initiated multiple checks generation");
        
        if (!formController.validateInput()) {
            LoggerUtil.logUserAction(logger, "generate_multiple_checks_validation_failed", "Input validation failed");
            return;
        }

        try {
            int numChecks = Integer.parseInt(formController.getNumChecksField().getText());
            String interval = formController.getIntervalComboBox().getValue();
            
            LoggerUtil.logOperationStart(logger, "generate_multiple_checks", numChecks, interval);
            
            ExceptionHandler.executeWithExceptionHandling(logger, "generateMultipleChecks", () -> {
                ChequeData baseData = formController.createChequeData();
                dataController.generateMultipleChecks(baseData, numChecks, interval);
                LoggerUtil.logOperationSuccess(logger, "generate_multiple_checks");
            }, "Failed to generate multiple cheques. Please check your input and try again.");
            
        } catch (NumberFormatException e) {
            ExceptionHandler.handleValidationException(logger, "number_of_checks", "Please enter a valid number of checks.");
        }
    }

    @FXML
    private void clearMultiChequeTable() {
        dataController.clearMultiChequeTable();
    }

    @FXML
    private void printAllCheques() {
        dataController.printAllCheques();
    }
    
    @FXML
    private void printAllChequesFromTable() {
        dataController.printAllChequesFromTable();
    }
    
    @FXML
    private void exportSelectedToExcel() {
        exportController.exportSelectedToExcel();
    }
    
    @FXML
    private void exportFilteredToExcel() {
        exportController.exportFilteredToExcel();
    }
    
    @FXML
    private void exportAllToExcel() {
        exportController.exportAllToExcel();
    }
    
    @FXML
    private void printPaymentPlan() {
        LoggerUtil.logUserAction(logger, "print_payment_plan", "User initiated payment plan printing");
        
        ExceptionHandler.executeWithExceptionHandling(logger, "printPaymentPlan", () -> {
            // Get cheques from the multiple cheque editing table
            List<ChequeData> cheques = new ArrayList<>(dataController.getMultiChequeData());
            
            if (cheques.isEmpty()) {
                ExceptionHandler.handleValidationException(logger, "payment_plan_data", "No cheques found in the editing table. Please generate multiple cheques first.");
                return;
            }
            
            LoggerUtil.logOperationStart(logger, "print_payment_plan", cheques.size());
            
            // Get signer name from the first cheque (assuming all cheques have the same signer)
            String signerName = cheques.get(0).getSignerName();
            
            try {
                // Generate payment plan PDF
                PDDocument document = PaymentPlanService.generatePaymentPlanPDF(cheques, signerName);
                
                // Print the PDF using dedicated A4 Portrait method for payment plan
                boolean printSuccessful = PdfPrinter.printPaymentPlanPdf(document);
                
                if (printSuccessful) {
                    logger.info("Payment plan printed successfully for {} cheques", cheques.size());
                } else {
                    logger.info("Payment plan print cancelled by user");
                    showAlert("Print Cancelled", "Payment plan print job was cancelled.");
                    return; // Exit without showing success message
                }
            } catch (Exception e) {
                throw new RuntimeException("Payment plan generation or printing failed", e);
            }
            
            LoggerUtil.logOperationSuccess(logger, "print_payment_plan");
            showAlert("Success", "Payment plan printed successfully! Total cheques: " + cheques.size());
            
        }, "Failed to print payment plan. Please check your printer settings and try again.");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private FilteredList<InMemoryLogAppender.LogEntry> filteredLogs;
    
    private void setupLogViewer() {
        if (logTable != null) {
            LoggerUtil.logMethodEntry(logger, "setupLogViewer");
            
            try {
                setupLogTableColumns();
                setupLogFilters();
                setupLogEventHandlers();
                refreshLogs();
                updateLogsDirectoryInfo();
                
                LoggerUtil.logMethodExit(logger, "setupLogViewer");
            } catch (Exception e) {
                LoggerUtil.logException(logger, "setupLogViewer", e);
            }
        }
    }
    
    private void setupLogTableColumns() {
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
    
    private void setupLogFilters() {
        // Setup level filter
        levelFilter.getItems().addAll("ALL", "ERROR", "WARN", "INFO", "DEBUG");
        levelFilter.setValue("ALL");
        
        // Setup filtered list
        filteredLogs = new FilteredList<>(InMemoryLogAppender.getLogEntries());
        logTable.setItems(filteredLogs);
        
        // Apply filters when changed
        levelFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyLogFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyLogFilters());
    }
    
    private void setupLogEventHandlers() {
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
    
    private void applyLogFilters() {
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
        
        if (logTable != null) {
            // The observable list is automatically updated, just need to refresh the table
            logTable.refresh();
            updateLogCount();
            
            // Sort by timestamp (newest first)
            logTable.sort();
        }
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
    
    @FXML
    private void cleanupOldLogs() {
        LoggerUtil.logUserAction(logger, "cleanup_old_logs", "User triggered manual log cleanup");
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cleanup Old Logs");
        confirm.setHeaderText("Delete Old Log Files");
        confirm.setContentText("This will delete all log files older than 7 days. Are you sure you want to continue?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Get info before cleanup
                LogCleanupService.LogsInfo beforeInfo = LogCleanupService.getLogsInfo();
                
                // Perform cleanup
                LogCleanupService.manualCleanup();
                
                // Get info after cleanup
                LogCleanupService.LogsInfo afterInfo = LogCleanupService.getLogsInfo();
                
                // Update the directory info display
                updateLogsDirectoryInfo();
                
                // Show result
                int deletedFiles = beforeInfo.getFileCount() - afterInfo.getFileCount();
                if (deletedFiles > 0) {
                    showAlert("Cleanup Complete",
                        String.format("Successfully deleted %d old log files.\nBefore: %s\nAfter: %s",
                        deletedFiles, beforeInfo.toString(), afterInfo.toString()));
                } else {
                    showAlert("Cleanup Complete", "No old log files found to delete.\nCurrent status: " + afterInfo.toString());
                }
                
            } catch (Exception e) {
                LoggerUtil.logException(logger, "cleanupOldLogs", e);
                showAlert("Cleanup Error", "Failed to cleanup old logs: " + e.getMessage());
            }
        }
    }
    
    private void updateLogsDirectoryInfo() {
        if (logsDirectoryInfo != null) {
            try {
                LogCleanupService.LogsInfo info = LogCleanupService.getLogsInfo();
                logsDirectoryInfo.setText(String.format("Log files: %s | Auto-cleanup: 7 days", info.toString()));
            } catch (Exception e) {
                logsDirectoryInfo.setText("Log files: logs/ | Auto-cleanup: 7 days");
            }
        }
    }
    
    private void copyToClipboard(String text) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    // Getters for sub-controllers (for testing or external access)
    public ChequeFormController getFormController() { return formController; }
    public ChequePreviewController getPreviewController() { return previewController; }
    public ChequePrintController getPrintController() { return printController; }
    public ChequeDataController getDataController() { return dataController; }
    public ChequeFilterController getFilterController() { return filterController; }
    public ChequeExportController getExportController() { return exportController; }
    public BankTemplateController getTemplateController() { return templateController; }

    @FXML
    private void handleJsonEditorMenuItem() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/chequePrinter/view/JsonEditorView.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("JSON Editor");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            ExceptionHandler.handleException(logger, "handleJsonEditorMenuItem", e, "Failed to open JSON Editor.");
        }
    }
}