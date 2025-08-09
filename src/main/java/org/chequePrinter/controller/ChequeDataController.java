package org.chequePrinter.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.chequePrinter.model.BankTemplate;
import org.chequePrinter.model.ChequeData;
import org.chequePrinter.service.DatabaseService;
import org.chequePrinter.util.ArabicNumberToWords;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChequeDataController {

    @FXML
    private TableView<ChequeData> chequeTableView;
    @FXML
    private TableColumn<ChequeData, Integer> idColumn;
    @FXML
    private TableColumn<ChequeData, String> dateColumn;
    @FXML
    private TableColumn<ChequeData, String> beneficiaryColumn;
    @FXML
    private TableColumn<ChequeData, String> amountNumericColumn;
    @FXML
    private TableColumn<ChequeData, String> amountWordsColumn;
    @FXML
    private TableColumn<ChequeData, String> signerColumn;
    @FXML
    private TableColumn<ChequeData, String> phoneNumberColumn;

    // Multiple cheque editing table
    @FXML
    private TableView<ChequeData> multiChequeTableView;
    @FXML
    private TableColumn<ChequeData, String> multiDateColumn;
    @FXML
    private TableColumn<ChequeData, String> multiBeneficiaryColumn;
    @FXML
    private TableColumn<ChequeData, String> multiAmountColumn;
    @FXML
    private TableColumn<ChequeData, String> multiAmountWordsColumn;
    @FXML
    private TableColumn<ChequeData, String> multiSignerColumn;
    @FXML
    private TableColumn<ChequeData, String> multiPhoneNumberColumn;
    @FXML
    private TableColumn<ChequeData, Void> multiActionColumn;

    private ObservableList<ChequeData> chequeDataList = FXCollections.observableArrayList();
    private ObservableList<ChequeData> multiChequeDataList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dbDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Callback interfaces for communication with other controllers
    private Runnable onChequeSelected;
    private ChequePrintController printController;
    private BankTemplateController templateController;

    public void initialize() {
        setupTableView();
        setupMultiChequeTableView();
        loadChequeRecords();
    }

    private void setupTableView() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        beneficiaryColumn.setCellValueFactory(new PropertyValueFactory<>("beneficiaryName"));
        amountNumericColumn.setCellValueFactory(new PropertyValueFactory<>("amountNumeric"));
        amountWordsColumn.setCellValueFactory(new PropertyValueFactory<>("amountWords"));
        signerColumn.setCellValueFactory(new PropertyValueFactory<>("signerName"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        dateColumn.setCellFactory(column -> new TableCell<ChequeData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(LocalDate.parse(item, dbDateFormatter).format(dateFormatter));
                }
            }
        });

        chequeTableView.setItems(chequeDataList);

        // Enable multiple selection
        chequeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        chequeTableView.setRowFactory(tv -> {
            TableRow<ChequeData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    ChequeData rowData = row.getItem();
                    if (onChequeSelected != null) {
                        onChequeSelected.run();
                    }
                }
            });
            return row;
        });
    }

    private void setupMultiChequeTableView() {
        // Setup cell value factories
        multiDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        multiBeneficiaryColumn.setCellValueFactory(new PropertyValueFactory<>("beneficiaryName"));
        multiAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amountNumeric"));
        multiAmountWordsColumn.setCellValueFactory(new PropertyValueFactory<>("amountWords"));
        multiSignerColumn.setCellValueFactory(new PropertyValueFactory<>("signerName"));
        multiPhoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        // Make table editable
        multiChequeTableView.setEditable(true);

        // Setup editable date column with DatePicker cell factory
        multiDateColumn.setCellFactory(column -> new DatePickerTableCell());
        multiDateColumn.setOnEditCommit(event -> {
            ChequeData cheque = event.getRowValue();
            String newDate = event.getNewValue();
            
            // Since DatePicker ensures valid dates, we can directly use the value
            try {
                LocalDate parsedDate = parseDate(newDate);
                String formattedDate = parsedDate.format(dbDateFormatter); // Store in yyyy-MM-dd format
                cheque.setDate(formattedDate);
                
                // Update preview only if this is the currently selected row
                ChequeData selectedCheque = multiChequeTableView.getSelectionModel().getSelectedItem();
                if (selectedCheque != null && selectedCheque == cheque) {
                    updatePreviewFromTable(cheque);
                }
                
                // Refresh the table to show the updated date
                multiChequeTableView.refresh();
            } catch (Exception e) {
                // This should rarely happen with DatePicker, but handle gracefully
                System.err.println("Error updating date: " + e.getMessage());
                multiChequeTableView.refresh();
            }
        });

        // Setup editable amount column with validation and custom display formatting
        multiAmountColumn.setCellFactory(column -> new TableCell<ChequeData, String>() {
            private TextField textField;
            
            @Override
            public void startEdit() {
                if (!isEmpty()) {
                    super.startEdit();
                    createTextField();
                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                    textField.requestFocus();
                }
            }
            
            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(formatAmountDisplay(getItem()));
                setGraphic(null);
            }
            
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        if (textField != null) {
                            textField.setText(formatAmountDisplay(getItem()));
                        }
                        setText(null);
                        setGraphic(textField);
                    } else {
                        setText(formatAmountDisplay(item));
                        setGraphic(null);
                    }
                }
            }
            
            private void createTextField() {
                textField = new TextField(formatAmountDisplay(getItem()));
                textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
                
                // Flag to prevent duplicate commit events
                final boolean[] isCommitting = {false};
                
                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused && !isCommitting[0]) {
                        isCommitting[0] = true;
                        commitEdit(textField.getText());
                    }
                });
                
                textField.setOnAction(evt -> {
                    if (!isCommitting[0]) {
                        isCommitting[0] = true;
                        commitEdit(textField.getText());
                    }
                });
                
                textField.setOnKeyPressed(event -> {
                    if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                });
            }
            
            private String formatAmountDisplay(String amount) {
                if (amount == null || amount.trim().isEmpty()) {
                    return "";
                }
                try {
                    // Parse and format to remove unnecessary decimals
                    double value = Double.parseDouble(amount);
                    if (value == Math.floor(value)) {
                        return String.valueOf((long) value);
                    } else {
                        return amount; // Keep original if it has meaningful decimals
                    }
                } catch (NumberFormatException e) {
                    return amount;
                }
            }
        });
        multiAmountColumn.setOnEditCommit(event -> {
            ChequeData cheque = event.getRowValue();
            String newAmount = event.getNewValue();
            
            // Validate amount
            if (isValidAmount(newAmount)) {
                // Convert to integer format (remove decimal points like .0)
                String cleanAmount = formatAmountAsInteger(newAmount);
                cheque.setAmountNumeric(cleanAmount);
                
                // Update amount in words for this specific cheque only
                try {
                    double amount = Double.parseDouble(cleanAmount);
                    String words = ArabicNumberToWords.convert(amount);
                    cheque.setAmountWords(words);
                } catch (NumberFormatException e) {
                    cheque.setAmountWords("");
                }
                
                // Update preview only if this is the currently selected row
                ChequeData selectedCheque = multiChequeTableView.getSelectionModel().getSelectedItem();
                if (selectedCheque != null && selectedCheque == cheque) {
                    updatePreviewFromTable(cheque);
                }
                
                // Refresh the table to show the updated amount and words
                multiChequeTableView.refresh();
            } else {
                showAlert("Invalid Amount", "Please enter a valid positive integer amount only.\nDecimals like 50.0 or 50.01 are not allowed.\nExample: 100, 250, 1000");
                // Revert to original value - no need to refresh as the edit will be cancelled automatically
                multiChequeTableView.refresh();
            }
        });

        // Setup action column with print button
        multiActionColumn.setCellFactory(param -> new TableCell<ChequeData, Void>() {
            private final Button printBtn = new Button("Print");

            {
                printBtn.setOnAction(event -> {
                    ChequeData cheque = getTableView().getItems().get(getIndex());
                    printSingleChequeFromTable(cheque);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(printBtn);
                }
            }
        });

        // Set items and enable row selection
        multiChequeTableView.setItems(multiChequeDataList);
        multiChequeTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updatePreviewFromTable(newSelection);
            }
        });
    }

    public void loadChequeRecords() {
        chequeDataList.clear();
        chequeDataList.addAll(DatabaseService.getAllCheques());
    }

    public void saveCheque(ChequeData chequeData) {
        // Convert date format for database storage
        ChequeData chequeToSave = new ChequeData(
            parseDate(chequeData.getDate()).format(dbDateFormatter),
            chequeData.getBeneficiaryName(),
            chequeData.getAmountNumeric(),
            chequeData.getAmountWords(),
            chequeData.getSignerName(),
            chequeData.getPhoneNumber()
        );
        DatabaseService.saveCheque(chequeToSave);
        loadChequeRecords();
    }

    public void saveCheques(List<ChequeData> chequeDataList) {
        for (ChequeData cheque : chequeDataList) {
            saveCheque(cheque);
        }
    }

    @FXML
    public void deleteSelectedRecord() {
        ObservableList<ChequeData> selectedItems = chequeTableView.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            showAlert("No Selection", "Please select one or more cheques to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Cheque Record(s)");
        confirm.setContentText("Are you sure you want to delete " + selectedItems.size() + " cheque record(s)?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Create a copy of the list to avoid concurrent modification
            List<ChequeData> itemsToDelete = new ArrayList<>(selectedItems);
            
            for (ChequeData cheque : itemsToDelete) {
                DatabaseService.deleteCheque(cheque.getId());
            }
            
            loadChequeRecords();
            showAlert("Success", itemsToDelete.size() + " cheque record(s) deleted.");
        }
    }

    @FXML
    public void generateMultipleChecks(ChequeData baseData, int numChecks, String interval) {
        try {
            LocalDate currentDate = parseDate(baseData.getDate());
            multiChequeDataList.clear();
            
            for (int i = 0; i < numChecks; i++) {
                ChequeData chequeData = new ChequeData();
                chequeData.setDate(currentDate.toString());
                chequeData.setBeneficiaryName(baseData.getBeneficiaryName());
                chequeData.setAmountNumeric(baseData.getAmountNumeric());
                chequeData.setAmountWords(baseData.getAmountWords());
                chequeData.setSignerName(baseData.getSignerName());
                chequeData.setPhoneNumber(baseData.getPhoneNumber());
                
                multiChequeDataList.add(chequeData);
                
                // Calculate next date based on month interval
                if (interval != null && interval.contains("Month")) {
                    String monthsStr = interval.split(" ")[0];
                    try {
                        int months = Integer.parseInt(monthsStr);
                        currentDate = currentDate.plusMonths(months);
                    } catch (NumberFormatException e) {
                        // Default to 1 month if parsing fails
                        currentDate = currentDate.plusMonths(1);
                    }
                } else {
                    // Default to 1 month
                    currentDate = currentDate.plusMonths(1);
                }
            }
            
            showAlert("Success", numChecks + " cheques generated in the table below. You can edit amounts and dates as needed.");
            
        } catch (Exception e) {
            showAlert("Error", "Failed to generate cheques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void clearMultiChequeTable() {
        multiChequeDataList.clear();
    }

    @FXML
    public void printAllCheques() {
        if (multiChequeDataList.isEmpty()) {
            showAlert("No Cheques", "No cheques to print.");
            return;
        }

        if (printController != null && templateController != null) {
            try {
                // Get the selected template from template controller
                BankTemplate.Template selectedTemplate = templateController.getSelectedTemplate();
                if (selectedTemplate == null) {
                    showAlert("Error", "Please select a valid bank template before printing.");
                    return;
                }

                // Convert ObservableList to regular List
                List<ChequeData> chequeList = new ArrayList<>(multiChequeDataList);

                // Generate single PDF with multiple pages (one page per cheque)
                PDDocument document = printController.generateMultipleChequePDF(chequeList, selectedTemplate);
                
                // Print the entire PDF as one document
                printController.printPDF(document, selectedTemplate.getWidth(), selectedTemplate.getHeight());
                
                // Save all cheques to database after successful printing
                saveCheques(chequeList);
                
                // Refresh the main table to show the newly saved records
                loadChequeRecords();
                
                showAlert("Success", chequeList.size() + " cheques printed as one PDF document and saved to database!");
                
            } catch (Exception e) {
                showAlert("Error", "Failed to print cheques: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Error", "Print controller or template controller not available.");
        }
    }

    @FXML
    public void printAllChequesFromTable() {
        ObservableList<ChequeData> selectedCheques = chequeTableView.getSelectionModel().getSelectedItems();
        
        if (selectedCheques.isEmpty()) {
            // If no selection, print all visible cheques
            selectedCheques = chequeTableView.getItems();
        }
        
        if (selectedCheques.isEmpty()) {
            showAlert("No Cheques", "No cheques to print.");
            return;
        }

        if (printController != null && templateController != null) {
            try {
                // Get the selected template from template controller
                BankTemplate.Template selectedTemplate = templateController.getSelectedTemplate();
                if (selectedTemplate == null) {
                    showAlert("Error", "Please select a valid bank template before printing.");
                    return;
                }

                // Convert ObservableList to regular List
                List<ChequeData> chequeList = new ArrayList<>(selectedCheques);

                // Generate single PDF with multiple pages (one page per cheque)
                PDDocument document = printController.generateMultipleChequePDF(chequeList, selectedTemplate);
                
                // Print the entire PDF as one document
                printController.printPDF(document, selectedTemplate.getWidth(), selectedTemplate.getHeight());
                
                // Note: These cheques are already in the database (from the main table)
                // But if they were edited in the multi-cheque table, we should update them
                // For now, we'll just refresh the table to ensure consistency
                loadChequeRecords();
                
                showAlert("Success", chequeList.size() + " cheques from table printed as one PDF document!");
                
            } catch (Exception e) {
                showAlert("Error", "Failed to print cheques from table: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Error", "Print controller or template controller not available.");
        }
    }

    private void printSingleChequeFromTable(ChequeData cheque) {
        if (printController != null && templateController != null) {
            try {
                // Get the selected template from template controller
                BankTemplate.Template selectedTemplate = templateController.getSelectedTemplate();
                if (selectedTemplate == null) {
                    showAlert("Error", "Please select a valid bank template before printing.");
                    return;
                }

                // Generate PDF and print
                PDDocument document = printController.generateChequePDF(cheque, selectedTemplate);
                printController.printPDF(document, selectedTemplate.getWidth(), selectedTemplate.getHeight());
                
                showAlert("Success", "Cheque printed successfully!");
                
            } catch (Exception e) {
                showAlert("Error", "Failed to print cheque: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Error", "Print controller or template controller not available.");
        }
    }

    private void updatePreviewFromTable(ChequeData cheque) {
        // This would be handled by a callback to the main controller
        // or through a preview controller reference
        if (onChequeSelected != null) {
            onChequeSelected.run();
        }
    }

    public ChequeData getSelectedCheque() {
        return chequeTableView.getSelectionModel().getSelectedItem();
    }

    public ObservableList<ChequeData> getSelectedCheques() {
        return chequeTableView.getSelectionModel().getSelectedItems();
    }

    public ObservableList<ChequeData> getAllCheques() {
        return chequeDataList;
    }

    public ObservableList<ChequeData> getMultiChequeData() {
        return multiChequeDataList;
    }

    public void setFilteredItems(ObservableList<ChequeData> filteredItems) {
        chequeTableView.setItems(filteredItems);
    }

    public void resetToAllItems() {
        chequeTableView.setItems(chequeDataList);
    }

    private LocalDate parseDate(String dateText) {
        if (dateText == null || dateText.trim().isEmpty()) {
            return LocalDate.now();
        }
        
        // Clean up the input - remove any non-printable characters, extra spaces, and unwanted characters
        String cleanedDate = dateText.trim()
                .replaceAll("[^\\d/\\-]", "") // Remove any character that's not a digit, slash, or dash
                .replaceAll("\\s+", ""); // Remove any whitespace
        
        if (cleanedDate.isEmpty()) {
            return LocalDate.now();
        }
        
        try {
            // Try parsing with ISO format first (yyyy-MM-dd)
            if (cleanedDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(cleanedDate);
            }
            
            // Try parsing with dd/MM/yyyy format
            if (cleanedDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
                return LocalDate.parse(cleanedDate, dateFormatter);
            }
            
            // Try parsing with yyyy/MM/dd format
            if (cleanedDate.matches("\\d{4}/\\d{2}/\\d{2}")) {
                return LocalDate.parse(cleanedDate.replace('/', '-'));
            }
            
            // Try parsing with dd-MM-yyyy format
            if (cleanedDate.matches("\\d{2}-\\d{2}-\\d{4}")) {
                String[] parts = cleanedDate.split("-");
                return LocalDate.of(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
            }
            
            // Try parsing with yyyy-MM-dd format (database format)
            if (cleanedDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(cleanedDate, dbDateFormatter);
            }
            
            // Default fallback - try ISO format
            return LocalDate.parse(cleanedDate);
            
        } catch (Exception e) {
            // Only log if the cleaned date is different from original (to avoid spam)
            if (!cleanedDate.equals(dateText.trim())) {
                System.err.println("Cleaned date input from '" + dateText + "' to '" + cleanedDate + "'");
            }
            System.err.println("Failed to parse date: " + cleanedDate + ", using current date instead");
            return LocalDate.now();
        }
    }

    private boolean isValidDate(String dateText) {
        if (dateText == null || dateText.trim().isEmpty()) {
            return false;
        }
        
        // Clean up the input first
        String cleanedDate = dateText.trim()
                .replaceAll("[^\\d/\\-]", "") // Remove any character that's not a digit, slash, or dash
                .replaceAll("\\s+", ""); // Remove any whitespace
        
        if (cleanedDate.isEmpty()) {
            return false;
        }
        
        try {
            // Validate date format patterns without calling parseDate to avoid multiple logging
            LocalDate parsedDate = null;
            
            // Try parsing with ISO format first (yyyy-MM-dd)
            if (cleanedDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                parsedDate = LocalDate.parse(cleanedDate);
            }
            // Try parsing with dd/MM/yyyy format
            else if (cleanedDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
                parsedDate = LocalDate.parse(cleanedDate, dateFormatter);
            }
            // Try parsing with yyyy/MM/dd format
            else if (cleanedDate.matches("\\d{4}/\\d{2}/\\d{2}")) {
                parsedDate = LocalDate.parse(cleanedDate.replace('/', '-'));
            }
            // Try parsing with dd-MM-yyyy format
            else if (cleanedDate.matches("\\d{2}-\\d{2}-\\d{4}")) {
                String[] parts = cleanedDate.split("-");
                parsedDate = LocalDate.of(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
            }
            else {
                return false; // No valid pattern matched
            }
            
            if (parsedDate == null) {
                return false;
            }
            
            // Additional validation: ensure date is not too far in the past or future
            LocalDate now = LocalDate.now();
            LocalDate minDate = now.minusYears(10); // 10 years ago
            LocalDate maxDate = now.plusYears(10);  // 10 years in future
            
            if (parsedDate.isBefore(minDate) || parsedDate.isAfter(maxDate)) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidAmount(String amountText) {
        if (amountText == null || amountText.trim().isEmpty()) {
            return false;
        }
        
        String trimmedAmount = amountText.trim();
        
        try {
            // Parse as double to validate it's a number
            double amount = Double.parseDouble(trimmedAmount);
            
            // Must be positive
            if (amount <= 0) {
                return false;
            }
            
            // Check if it's actually an integer value (no fractional part)
            if (amount != Math.floor(amount)) {
                return false; // Has fractional part like 500.50
            }
            
            // Check reasonable range (not too large)
            if (amount > 999999999) { // Max 999 million
                return false;
            }
            
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String formatAmountAsInteger(String amountText) {
        if (amountText == null || amountText.trim().isEmpty()) {
            return amountText;
        }
        
        try {
            // Parse as double to handle decimal inputs
            double amount = Double.parseDouble(amountText.trim());
            
            // Convert to integer (remove decimal part)
            long integerAmount = (long) amount;
            
            // Return as string without decimal point
            return String.valueOf(integerAmount);
        } catch (NumberFormatException e) {
            // If parsing fails, return original
            return amountText;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Setters for dependency injection
    public void setOnChequeSelected(Runnable callback) {
        this.onChequeSelected = callback;
    }

    public void setPrintController(ChequePrintController printController) {
        this.printController = printController;
    }

    public void setTemplateController(BankTemplateController templateController) {
        this.templateController = templateController;
    }

    // Getters for table views
    public TableView<ChequeData> getChequeTableView() { return chequeTableView; }
    public TableView<ChequeData> getMultiChequeTableView() { return multiChequeTableView; }

    // Custom TableCell for DatePicker date input
    private class DatePickerTableCell extends TableCell<ChequeData, String> {
        private DatePicker datePicker;
        private String originalValue;

        public DatePickerTableCell() {
            super();
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                originalValue = getItem(); // Store original value before editing
                createDatePicker();
                setText(null);
                setGraphic(datePicker);
                datePicker.requestFocus();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            // Revert to original value display format
            setText(getDisplayText(originalValue));
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (datePicker != null) {
                        LocalDate date = parseDate(getItem());
                        datePicker.setValue(date);
                    }
                    setText(null);
                    setGraphic(datePicker);
                } else {
                    setText(getDisplayText(item));
                    setGraphic(null);
                }
            }
        }

        private String getDisplayText(String dateValue) {
            if (dateValue == null || dateValue.isEmpty()) {
                return "";
            }
            
            try {
                LocalDate date = parseDate(dateValue);
                return date.format(dateFormatter);
            } catch (Exception e) {
                // If parsing fails for display, just return the original value
                return dateValue;
            }
        }

        private void createDatePicker() {
            datePicker = new DatePicker();
            
            // Set initial value
            try {
                LocalDate currentDate = parseDate(getItem());
                datePicker.setValue(currentDate);
            } catch (Exception e) {
                datePicker.setValue(LocalDate.now());
            }
            
            // Set reasonable date range (10 years back and forward)
            LocalDate now = LocalDate.now();
            datePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    LocalDate minDate = now.minusYears(10);
                    LocalDate maxDate = now.plusYears(10);
                    setDisable(empty || date.isBefore(minDate) || date.isAfter(maxDate));
                }
            });
            
            // Handle value changes
            datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
                if (newDate != null) {
                    // Convert to database format and commit
                    String formattedDate = newDate.format(dbDateFormatter);
                    commitEdit(formattedDate);
                }
            });
            
            // Handle focus lost
            datePicker.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused && isEditing()) {
                    LocalDate selectedDate = datePicker.getValue();
                    if (selectedDate != null) {
                        String formattedDate = selectedDate.format(dbDateFormatter);
                        commitEdit(formattedDate);
                    } else {
                        cancelEdit();
                    }
                }
            });
            
            // Handle Enter key
            datePicker.setOnAction(evt -> {
                LocalDate selectedDate = datePicker.getValue();
                if (selectedDate != null) {
                    String formattedDate = selectedDate.format(dbDateFormatter);
                    commitEdit(formattedDate);
                } else {
                    cancelEdit();
                }
            });
            
            // Handle Escape key
            datePicker.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                    cancelEdit();
                }
            });
        }
    }
}