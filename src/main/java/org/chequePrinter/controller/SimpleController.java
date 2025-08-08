package org.chequePrinter.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.chequePrinter.model.ChequeData;
import org.chequePrinter.model.PdfContent;
import org.chequePrinter.model.BankTemplate;
import org.chequePrinter.service.DatabaseService;
import org.chequePrinter.service.PdfGenerator;
import org.chequePrinter.service.PdfPrinter;
import org.chequePrinter.util.ArabicNumberToWords;
import org.chequePrinter.util.JsonLoader;

import com.lowagie.text.Element;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class SimpleController implements Initializable {

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

    @FXML private TableView<ChequeData> chequeTableView;
    @FXML private TableColumn<ChequeData, Integer> idColumn;
    @FXML private TableColumn<ChequeData, String> dateColumn;
    @FXML private TableColumn<ChequeData, String> beneficiaryColumn;
    @FXML private TableColumn<ChequeData, String> amountNumericColumn;
    @FXML private TableColumn<ChequeData, String> amountWordsColumn;
    @FXML private TableColumn<ChequeData, String> signerColumn;

    @FXML private TextField filterBeneficiaryField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    // Multiple cheque editing table
    @FXML private TableView<ChequeData> multiChequeTableView;
    @FXML private TableColumn<ChequeData, String> multiDateColumn;
    @FXML private TableColumn<ChequeData, String> multiBeneficiaryColumn;
    @FXML private TableColumn<ChequeData, String> multiAmountColumn;
    @FXML private TableColumn<ChequeData, String> multiAmountWordsColumn;
    @FXML private TableColumn<ChequeData, String> multiSignerColumn;
    @FXML private TableColumn<ChequeData, Void> multiActionColumn;

    private ObservableList<ChequeData> chequeDataList = FXCollections.observableArrayList();
    private ObservableList<ChequeData> multiChequeDataList = FXCollections.observableArrayList();
    private java.util.List<BankTemplate> bankTemplates;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadBankConfiguration();
        setupUI();
        setupTableView();
        setupMultiChequeTableView();
        setupEventHandlers();
        loadChequeRecords();
    }

    private void loadBankConfiguration() {
        try {
            bankTemplates = JsonLoader.loadBankTemplates("/bank.json");
            System.out.println("Bank configuration loaded successfully: " + bankTemplates.size() + " banks");
        } catch (Exception e) {
            System.err.println("Failed to load bank configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupUI() {
        // Set default values
        firstCheckDatePicker.setValue(LocalDate.now());
        numChecksField.setText("1");
        
        // Setup interval combo box
        intervalComboBox.getItems().addAll("1 Month", "2 Months", "3 Months", "4 Months", "5 Months", "6 Months",
                                          "7 Months", "8 Months", "9 Months", "10 Months", "11 Months", "12 Months");
        intervalComboBox.setValue("1 Month");
        
        // Setup bank combo box
        if (bankTemplates != null && !bankTemplates.isEmpty()) {
            bankComboBox.setItems(FXCollections.observableArrayList(bankTemplates));
            bankComboBox.setValue(bankTemplates.get(0));
            updateTemplateComboBox(bankTemplates.get(0));
        }
        
        // Setup bank selection handler
        bankComboBox.setOnAction(e -> {
            BankTemplate selectedBank = bankComboBox.getValue();
            if (selectedBank != null) {
                updateTemplateComboBox(selectedBank);
            }
        });
        
        // Setup template selection handler
        templateComboBox.setOnAction(e -> {
            BankTemplate.Template selectedTemplate = templateComboBox.getValue();
            if (selectedTemplate != null) {
                
            }
        });
    }
    
    private void updateTemplateComboBox(BankTemplate bank) {
        if (bank != null && bank.getTemplates() != null) {
            templateComboBox.setItems(FXCollections.observableArrayList(bank.getTemplates()));
            if (!bank.getTemplates().isEmpty()) {
                templateComboBox.setValue(bank.getTemplates().get(0));
                loadTemplateImageForBank(bank.getTemplates().get(0));
            }
        }
    }
    
    private void loadTemplateImageForBank(BankTemplate.Template template) {
        try {
            if (template != null && template.getImagePath() != null) {
                InputStream imageStream = getClass().getResourceAsStream(template.getImagePath());
                if (imageStream != null) {
                    Image image = new Image(imageStream);
                    chequeImageView.setImage(image);
                    imageStream.close();
                    System.out.println("Loaded template image: " + template.getImagePath());
                } else {
                    System.err.println("Template image not found: " + template.getImagePath());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading template image: " + e.getMessage());
        }
    }

    private void setupTableView() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        beneficiaryColumn.setCellValueFactory(new PropertyValueFactory<>("beneficiaryName"));
        amountNumericColumn.setCellValueFactory(new PropertyValueFactory<>("amountNumeric"));
        amountWordsColumn.setCellValueFactory(new PropertyValueFactory<>("amountWords"));
        signerColumn.setCellValueFactory(new PropertyValueFactory<>("signerName"));

        // Enable multiple selection
        chequeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        chequeTableView.setItems(chequeDataList);
    }

    private void setupMultiChequeTableView() {
        // Setup cell value factories
        multiDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        multiBeneficiaryColumn.setCellValueFactory(new PropertyValueFactory<>("beneficiaryName"));
        multiAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amountNumeric"));
        multiAmountWordsColumn.setCellValueFactory(new PropertyValueFactory<>("amountWords"));
        multiSignerColumn.setCellValueFactory(new PropertyValueFactory<>("signerName"));

        // Make table editable
        multiChequeTableView.setEditable(true);

        // Setup editable date column
        multiDateColumn.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        multiDateColumn.setOnEditCommit(event -> {
            ChequeData cheque = event.getRowValue();
            cheque.setDate(event.getNewValue());
            
            // Update preview only if this is the currently selected row
            ChequeData selectedCheque = multiChequeTableView.getSelectionModel().getSelectedItem();
            if (selectedCheque != null && selectedCheque == cheque) {
                updatePreviewFromTable(cheque);
            }
            
            // Refresh the table to show the updated date
            multiChequeTableView.refresh();
        });

        // Setup editable amount column
        multiAmountColumn.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        multiAmountColumn.setOnEditCommit(event -> {
            ChequeData cheque = event.getRowValue();
            String newAmount = event.getNewValue();
            cheque.setAmountNumeric(newAmount);
            
            // Update amount in words for this specific cheque only
            try {
                double amount = Double.parseDouble(newAmount);
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

    private void setupEventHandlers() {
        // Update preview when fields change
        beneficiaryField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateAmountWords();
            updatePreview();
        });
        signerField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        firstCheckDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        
        // Setup filtering listeners
        filterBeneficiaryField.textProperty().addListener((obs, oldVal, newVal) -> applyDateFilter());
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyDateFilter());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyDateFilter());
    }

    private void updateAmountWords() {
        try {
            String amountStr = amountField.getText();
            if (amountStr != null && !amountStr.trim().isEmpty()) {
                double amount = Double.parseDouble(amountStr);
                String words = ArabicNumberToWords.convert(amount);
                amountWordsField.setText(words);
            } else {
                amountWordsField.clear();
            }
        } catch (NumberFormatException e) {
            amountWordsField.clear();
        }
    }

    private void updatePreview() {
        // Update preview text elements
        if (firstCheckDatePicker.getValue() != null) {
            // Format date according to template's dateFormat
            BankTemplate.Template selectedTemplate = templateComboBox.getValue();
            String formattedDate = firstCheckDatePicker.getValue().toString();
            if (selectedTemplate != null && selectedTemplate.getDateFormat() != null) {
                LocalDate date = firstCheckDatePicker.getValue();
                switch (selectedTemplate.getDateFormat()) {
                    case "YYYY/MM/DD":
                        formattedDate = String.format("%04d/%02d/%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
                        break;
                    case "DD MM YYYY":
                        formattedDate = String.format("%02d %02d %04d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
                        break;
                    case "DD/MM/YYYY":
                        formattedDate = String.format("%02d/%02d/%04d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
                        break;
                    default:
                        break;
                }
            }
            dateText.setText(formattedDate);
        }
        beneficiaryText.setText(beneficiaryField.getText());
        amountWordsText.setText(amountWordsField.getText());
        amountText.setText(amountField.getText());
        signerText.setText(signerField.getText());

        // Position text elements using template-specific positioning if available
        BankTemplate.Template selectedTemplate = templateComboBox.getValue();
        if (selectedTemplate != null) {
            BankTemplate.Field datePos = selectedTemplate.getFields().get("dateField");
            if (datePos != null) {
                dateText.setX(datePos.getX());
                dateText.setY(datePos.getY());
            }
            
            BankTemplate.Field beneficiaryPos = selectedTemplate.getFields().get("beneficiaryField");
            if (beneficiaryPos != null) {
                beneficiaryText.setX(beneficiaryPos.getX());
                beneficiaryText.setY(beneficiaryPos.getY());
            }
            
            BankTemplate.Field amountWordsPos = selectedTemplate.getFields().get("amountWordsField");
            if (amountWordsPos != null) {
                amountWordsText.setX(amountWordsPos.getX());
                amountWordsText.setY(amountWordsPos.getY());
            }
            
            BankTemplate.Field amountPos = selectedTemplate.getFields().get("amountField");
            if (amountPos != null) {
                amountText.setX(amountPos.getX());
                amountText.setY(amountPos.getY());
            }
            
            BankTemplate.Field signerPos = selectedTemplate.getFields().get("signerField");
            if (signerPos != null) {
                signerText.setX(signerPos.getX());
                signerText.setY(signerPos.getY());
            }
        } else {
            // Fallback to default positioning
            dateText.setX(370);
            dateText.setY(200);
            beneficiaryText.setX(180);
            beneficiaryText.setY(160);
            amountWordsText.setX(140);
            amountWordsText.setY(135);
            amountText.setX(395);
            amountText.setY(120);
            signerText.setX(350);
            signerText.setY(90);
        }
    }

    @FXML
    private void printAndSaveSingleCheck() {
        if (!validateInput()) return;

        try {
            ChequeData chequeData = createChequeData();

            BankTemplate.Template selectedTemplate = templateComboBox.getValue();
            if (selectedTemplate == null) {
                showAlert("Error", "Please select a valid bank template before printing.");
                return;
            }

            float widthInCm = selectedTemplate.getWidth();
            float heightInCm = selectedTemplate.getHeight();

            // Generate PDF and print
            PDDocument document = generateChequePDF(chequeData);


            // Print the PDF
            org.chequePrinter.service.PdfPrinter.printPdf(document, widthInCm, heightInCm);
            
            // Save to database
            DatabaseService.saveCheque(chequeData);
            
            // Refresh table
            loadChequeRecords();
            
            showAlert("Success", "Cheque printed and saved successfully!");
            
        } catch (Exception e) {
            showAlert("Error", "Failed to print cheque: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void printAndSaveMultipleChecks() {
        if (!validateInput()) return;

        try {
            int numChecks = Integer.parseInt(numChecksField.getText());
            String interval = intervalComboBox.getValue();
            
            LocalDate currentDate = firstCheckDatePicker.getValue();
            java.util.List<java.util.List<PdfContent>> allPagesContent = new java.util.ArrayList<>();
            
            for (int i = 0; i < numChecks; i++) {
                ChequeData chequeData = createChequeData();
                chequeData.setDate(currentDate.toString());
                
                // Create PDF content for this cheque
                java.util.List<PdfContent> pageContent = createPdfContentForCheque(chequeData);
                allPagesContent.add(pageContent);
                
                // Save to database
                DatabaseService.saveCheque(chequeData);
                
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
            
            // --- CONVERSION ---
            // 1 inch = 72 points. 1 inch = 2.54 cm.
            // Points per cm = 72 / 2.54 = 28.346
            final float POINTS_PER_CM = 72f / 2.54f;

            // Get selected template for dimensions and background image
            BankTemplate.Template selectedTemplate = templateComboBox.getValue();
            float pageWidthInPoints, pageHeightInPoints;
            String templateImagePath = null;
            
            if (selectedTemplate != null) {
                // --- DESIRED DIMENSIONS (from bank.json in cm) ---
                float widthInCm = selectedTemplate.getWidth();
                float heightInCm = selectedTemplate.getHeight();
                
                // --- CALCULATED DIMENSIONS IN POINTS ---
                pageWidthInPoints = widthInCm * POINTS_PER_CM;
                pageHeightInPoints = heightInCm * POINTS_PER_CM;
                
                templateImagePath = selectedTemplate.getImagePath();
                System.out.println("=== MULTIPLE CHEQUES DIMENSIONS DEBUG ===");
                System.out.println("Template: " + selectedTemplate.getTemplateName());
                System.out.println("Width from bank.json: " + widthInCm + " cm");
                System.out.println("Height from bank.json: " + heightInCm + " cm");
                System.out.println("POINTS_PER_CM: " + POINTS_PER_CM);
                System.out.println("Calculated width in points: " + pageWidthInPoints);
                System.out.println("Calculated height in points: " + pageHeightInPoints);
                System.out.println("Template image: " + templateImagePath);
                System.out.println("=========================================");
            } else {
                // Fallback to default dimensions
                float widthInCm = 16.7f;
                float heightInCm = 8.1f;
                pageWidthInPoints = widthInCm * POINTS_PER_CM;
                pageHeightInPoints = heightInCm * POINTS_PER_CM;
                System.out.println("Using default dimensions for multiple cheques: " + widthInCm + "cm x " + heightInCm + "cm");
                System.out.println("Converted to points: " + pageWidthInPoints + " x " + pageHeightInPoints);
            }
            
            PDDocument document = PdfGenerator.generatePdf(pageWidthInPoints, pageHeightInPoints,
                                                         allPagesContent, "Amiri-Regular.ttf", templateImagePath);
            
            // Print the PDF
            org.chequePrinter.service.PdfPrinter.printPdf(document, selectedTemplate.getWidth(), selectedTemplate.getHeight());
            
            loadChequeRecords();
            showAlert("Success", numChecks + " cheques printed and saved successfully!");
            
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number of checks.");
        } catch (Exception e) {
            showAlert("Error", "Failed to print cheques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteSelectedRecord() {
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
            java.util.List<ChequeData> itemsToDelete = new java.util.ArrayList<>(selectedItems);
            
            for (ChequeData cheque : itemsToDelete) {
                DatabaseService.deleteCheque(cheque.getId());
            }
            
            loadChequeRecords();
            showAlert("Success", itemsToDelete.size() + " cheque record(s) deleted.");
        }
    }

    @FXML
    private void applyDateFilter() {
        FilteredList<ChequeData> filteredData = new FilteredList<>(FXCollections.observableArrayList(DatabaseService.getAllCheques()), p -> true);

        filteredData.setPredicate(cheque -> {
            // Date filtering
            boolean dateFilter = true;
            if (startDatePicker.getValue() != null || endDatePicker.getValue() != null) {
                try {
                    LocalDate chequeDate = LocalDate.parse(cheque.getDate());
                    LocalDate startDate = startDatePicker.getValue();
                    LocalDate endDate = endDatePicker.getValue();
                    
                    // Check start date condition
                    if (startDate != null && chequeDate.isBefore(startDate)) {
                        dateFilter = false;
                    }
                    
                    // Check end date condition
                    if (endDate != null && chequeDate.isAfter(endDate)) {
                        dateFilter = false;
                    }
                } catch (Exception e) {
                    // If date parsing fails, exclude this record
                    dateFilter = false;
                }
            }

            // Signer name filtering
            boolean signerFilter = true;
            String filterText = filterBeneficiaryField.getText();
            if (filterText != null && !filterText.trim().isEmpty()) {
                String signerName = cheque.getSignerName();
                if (signerName == null || !signerName.toLowerCase().contains(filterText.toLowerCase().trim())) {
                    signerFilter = false;
                }
            }

            return dateFilter && signerFilter;
        });

        chequeTableView.setItems(filteredData);
    }

    @FXML
    private void clearFilters() {
        filterBeneficiaryField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        // Reset to show all records
        chequeTableView.setItems(chequeDataList);
    }

    private ChequeData createChequeData() {
        ChequeData chequeData = new ChequeData();
        chequeData.setDate(firstCheckDatePicker.getValue().toString());
        chequeData.setBeneficiaryName(beneficiaryField.getText());
        chequeData.setAmountNumeric(amountField.getText());
        chequeData.setAmountWords(amountWordsField.getText());
        chequeData.setSignerName(signerField.getText());
        return chequeData;
    }

    private boolean validateInput() {
        if (beneficiaryField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter beneficiary name.");
            return false;
        }
        if (amountField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter amount.");
            return false;
        }
        if (signerField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter signer name.");
            return false;
        }
        if (firstCheckDatePicker.getValue() == null) {
            showAlert("Validation Error", "Please select a date.");
            return false;
        }
        
        try {
            Double.parseDouble(amountField.getText());
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid amount.");
            return false;
        }
        
        return true;
    }

    private void loadChequeRecords() {
        chequeDataList.clear();
        chequeDataList.addAll(DatabaseService.getAllCheques());
        // Apply current filters if any are active
        if ((filterBeneficiaryField.getText() != null && !filterBeneficiaryField.getText().trim().isEmpty()) ||
            startDatePicker.getValue() != null || endDatePicker.getValue() != null) {
            applyDateFilter();
        }
    }

    private PDDocument generateChequePDF(ChequeData chequeData) throws Exception {
        java.util.List<PdfContent> contentList = createPdfContentForCheque(chequeData);
        java.util.List<java.util.List<PdfContent>> allPagesContent = new java.util.ArrayList<>();
        allPagesContent.add(contentList);

        // --- CONVERSION ---
        // 1 inch = 72 points. 1 inch = 2.54 cm.
        // Points per cm = 72 / 2.54 = 28.346
        final float POINTS_PER_CM = 72f / 2.54f;

        // Get selected template for dimensions and background image
        BankTemplate.Template selectedTemplate = templateComboBox.getValue();
        float pageWidthInPoints, pageHeightInPoints;
        String templateImagePath = null;
        
        if (selectedTemplate != null) {
            // --- DESIRED DIMENSIONS (from bank.json in cm) ---
            float widthInCm = selectedTemplate.getWidth();
            float heightInCm = selectedTemplate.getHeight();
            
            // --- CALCULATED DIMENSIONS IN POINTS ---
            pageWidthInPoints = widthInCm * POINTS_PER_CM;
            pageHeightInPoints = heightInCm * POINTS_PER_CM;
            
            templateImagePath = selectedTemplate.getImagePath();
            System.out.println("=== SINGLE CHEQUE DIMENSIONS DEBUG ===");
            System.out.println("Template: " + selectedTemplate.getTemplateName());
            System.out.println("Width from bank.json: " + widthInCm + " cm");
            System.out.println("Height from bank.json: " + heightInCm + " cm");
            System.out.println("POINTS_PER_CM: " + POINTS_PER_CM);
            System.out.println("Calculated width in points: " + pageWidthInPoints);
            System.out.println("Calculated height in points: " + pageHeightInPoints);
            System.out.println("Template image: " + templateImagePath);
            System.out.println("=====================================");
        } else {
            // Fallback to default dimensions
            float widthInCm = 16.7f;
            float heightInCm = 8.1f;
            pageWidthInPoints = widthInCm * POINTS_PER_CM;
            pageHeightInPoints = heightInCm * POINTS_PER_CM;
            System.out.println("Using default dimensions: " + widthInCm + "cm x " + heightInCm + "cm");
            System.out.println("Converted to points: " + pageWidthInPoints + " x " + pageHeightInPoints);
        }

        return PdfGenerator.generatePdf(pageWidthInPoints, pageHeightInPoints,
                                      allPagesContent, "Amiri-Regular.ttf", templateImagePath);
    }

    private java.util.List<PdfContent> createPdfContentForCheque(ChequeData chequeData) {
        java.util.List<PdfContent> contentList = new java.util.ArrayList<>();

        // Get selected template for field positioning
        BankTemplate.Template selectedTemplate = templateComboBox.getValue();
        if (selectedTemplate == null) {
            // Fallback to default NBE template positioning
            System.out.println("Using default template positioning");
            contentList.add(new PdfContent(chequeData.getDate(), 14, Element.ALIGN_CENTER, 370, 200, 120f, 23f));
            contentList.add(new PdfContent(chequeData.getBeneficiaryName(), 14, Element.ALIGN_CENTER, 180, 160, 150f, 30f));
            contentList.add(new PdfContent(chequeData.getAmountWords(), 14, Element.ALIGN_CENTER, 140, 135, 300f, 30f));
            contentList.add(new PdfContent(chequeData.getAmountNumeric(), 14, Element.ALIGN_CENTER, 395, 120, 88f, 23f));
            contentList.add(new PdfContent(chequeData.getSignerName(), 14, Element.ALIGN_CENTER, 350, 90, 88f, 23f));
        } else {
            // Use template-specific field positions
            System.out.println("Using template-specific positioning for: " + selectedTemplate.getTemplateName());
            
            BankTemplate.Field datePos = selectedTemplate.getFields().get("dateField");
            if (datePos != null) {
                // Format date according to template's dateFormat
                String dateText = chequeData.getDate();
                String formattedDate = dateText;
                String templateDateFormat = selectedTemplate.getDateFormat();
                try {
                    LocalDate parsedDate = null;
                    // Try parsing with common formats
                    if (dateText.matches("\\d{4}[-/]\\d{2}[-/]\\d{2}")) {
                        parsedDate = LocalDate.parse(dateText.replace('/', '-'));
                    } else if (dateText.matches("\\d{2}[-/]\\d{2}[-/]\\d{4}")) {
                        String[] parts = dateText.split("[-/]");
                        parsedDate = LocalDate.of(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
                    }
                    if (parsedDate != null && templateDateFormat != null) {
                        switch (templateDateFormat) {
                            case "YYYY/MM/DD":
                                formattedDate = String.format("%04d/%02d/%02d", parsedDate.getYear(), parsedDate.getMonthValue(), parsedDate.getDayOfMonth());
                                break;
                            case "DD MM YYYY":
                                formattedDate = String.format("%02d %02d %04d", parsedDate.getDayOfMonth(), parsedDate.getMonthValue(), parsedDate.getYear());
                                break;
                            case "DD/MM/YYYY":
                                formattedDate = String.format("%02d/%02d/%04d", parsedDate.getDayOfMonth(), parsedDate.getMonthValue(), parsedDate.getYear());
                                break;
                            default:
                                // fallback to raw
                                break;
                        }
                    }
                } catch (Exception e) {
                    // fallback to raw dateText
                }
                contentList.add(new PdfContent(formattedDate, datePos.getFontSize(), datePos.getAlignment(),
                    datePos.getX(), datePos.getY(), datePos.getWidth(), 23f));
                System.out.println("Date field: '" + dateText + "' (length: " + dateText.length() + ") at (" + datePos.getX() + ", " + datePos.getY() + ")");
            }
            
            BankTemplate.Field beneficiaryPos = selectedTemplate.getFields().get("beneficiaryField");
            if (beneficiaryPos != null) {
                String beneficiaryText = chequeData.getBeneficiaryName();
                contentList.add(new PdfContent(beneficiaryText, beneficiaryPos.getFontSize(), beneficiaryPos.getAlignment(),
                    beneficiaryPos.getX(), beneficiaryPos.getY(), beneficiaryPos.getWidth(), 30f));
                System.out.println("Beneficiary field: '" + beneficiaryText + "' (length: " + beneficiaryText.length() + ") at (" + beneficiaryPos.getX() + ", " + beneficiaryPos.getY() + ")");
                // Print character codes for debugging
                for (int i = 0; i < beneficiaryText.length(); i++) {
                    System.out.print("U+" + Integer.toHexString(beneficiaryText.charAt(i)).toUpperCase() + " ");
                }
                System.out.println();
            }
            
            BankTemplate.Field amountWordsPos = selectedTemplate.getFields().get("amountWordsField");
            if (amountWordsPos != null) {
                String amountWordsText = chequeData.getAmountWords();
                contentList.add(new PdfContent(amountWordsText, amountWordsPos.getFontSize(), amountWordsPos.getAlignment(),
                    amountWordsPos.getX(), amountWordsPos.getY(), amountWordsPos.getWidth(), 30f));
                System.out.println("Amount words field: '" + amountWordsText + "' (length: " + amountWordsText.length() + ") at (" + amountWordsPos.getX() + ", " + amountWordsPos.getY() + ")");
                // Print character codes for debugging
                for (int i = 0; i < amountWordsText.length(); i++) {
                    System.out.print("U+" + Integer.toHexString(amountWordsText.charAt(i)).toUpperCase() + " ");
                }
                System.out.println();
            }
            
            BankTemplate.Field amountNumericPos = selectedTemplate.getFields().get("amountField");
            if (amountNumericPos != null) {
                String amountNumericText = chequeData.getAmountNumeric();
                contentList.add(new PdfContent(amountNumericText, amountNumericPos.getFontSize(), amountNumericPos.getAlignment(),
                    amountNumericPos.getX(), amountNumericPos.getY(), amountNumericPos.getWidth(), 23f));
                System.out.println("Amount numeric field: '" + amountNumericText + "' (length: " + amountNumericText.length() + ") at (" + amountNumericPos.getX() + ", " + amountNumericPos.getY() + ")");
            }
            
            BankTemplate.Field signerPos = selectedTemplate.getFields().get("signerField");
            if (signerPos != null) {
                String signerText = chequeData.getSignerName();
                contentList.add(new PdfContent(signerText, signerPos.getFontSize(), signerPos.getAlignment(),
                    signerPos.getX(), signerPos.getY(), signerPos.getWidth(), 23f));
                System.out.println("Signer field: '" + signerText + "' (length: " + signerText.length() + ") at (" + signerPos.getX() + ", " + signerPos.getY() + ")");
                // Print character codes for debugging
                for (int i = 0; i < signerText.length(); i++) {
                    System.out.print("U+" + Integer.toHexString(signerText.charAt(i)).toUpperCase() + " ");
                }
                System.out.println();
            }
        }

        // Add fixed text field if present (for printing/PDF only)
        if (selectedTemplate != null && selectedTemplate.getFixedTextField() != null) {
            BankTemplate.FixedTextField fixedText = selectedTemplate.getFixedTextField();
            if (fixedText.getText() != null && !fixedText.getText().trim().isEmpty()) {
                contentList.add(new PdfContent(
                    fixedText.getText(),
                    fixedText.getFontSize(),
                    fixedText.getAlignment(),
                    fixedText.getX(),
                    fixedText.getY(),
                    fixedText.getWidth(),
                    0));
                System.out.println("Fixed text field: '" + fixedText.getText() + "' at (" + fixedText.getX() + ", " + fixedText.getY() + ")");
            }
        }

        System.out.println("Created " + contentList.size() + " PDF content items for printing");
        return contentList;
    }

    private void updatePreviewFromTable(ChequeData cheque) {
        // Update the form fields with the selected cheque data
        try {
            firstCheckDatePicker.setValue(LocalDate.parse(cheque.getDate()));
        } catch (Exception e) {
            // Handle date parsing error
            firstCheckDatePicker.setValue(LocalDate.now());
        }
        beneficiaryField.setText(cheque.getBeneficiaryName());
        amountField.setText(cheque.getAmountNumeric());
        amountWordsField.setText(cheque.getAmountWords());
        signerField.setText(cheque.getSignerName());
        
        // This will trigger the preview update through the existing listeners
        updatePreview();
    }

    private void printSingleChequeFromTable(ChequeData cheque) {
        try {
            BankTemplate.Template selectedTemplate = templateComboBox.getValue();
            if (selectedTemplate == null) {
                showAlert("Error", "Please select a valid bank template before printing.");
                return;
            }

            float widthInCm = selectedTemplate.getWidth();
            float heightInCm = selectedTemplate.getHeight();

            // Generate PDF and print
            PDDocument document = generateChequePDFFromData(cheque);

            // Print the PDF
            org.chequePrinter.service.PdfPrinter.printPdf(document, widthInCm, heightInCm);
            
            showAlert("Success", "Cheque printed successfully!");
            
        } catch (Exception e) {
            showAlert("Error", "Failed to print cheque: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private PDDocument generateChequePDFFromData(ChequeData chequeData) throws Exception {
        java.util.List<PdfContent> contentList = createPdfContentForCheque(chequeData);
        java.util.List<java.util.List<PdfContent>> allPagesContent = new java.util.ArrayList<>();
        allPagesContent.add(contentList);

        // --- CONVERSION ---
        // 1 inch = 72 points. 1 inch = 2.54 cm.
        // Points per cm = 72 / 2.54 = 28.346
        final float POINTS_PER_CM = 72f / 2.54f;

        // Get selected template for dimensions and background image
        BankTemplate.Template selectedTemplate = templateComboBox.getValue();
        float pageWidthInPoints, pageHeightInPoints;
        String templateImagePath = null;
        
        if (selectedTemplate != null) {
            // --- DESIRED DIMENSIONS (from bank.json in cm) ---
            float widthInCm = selectedTemplate.getWidth();
            float heightInCm = selectedTemplate.getHeight();
            
            // --- CALCULATED DIMENSIONS IN POINTS ---
            pageWidthInPoints = widthInCm * POINTS_PER_CM;
            pageHeightInPoints = heightInCm * POINTS_PER_CM;
            
            templateImagePath = selectedTemplate.getImagePath();
        } else {
            // Fallback to default dimensions
            float widthInCm = 16.7f;
            float heightInCm = 8.1f;
            pageWidthInPoints = widthInCm * POINTS_PER_CM;
            pageHeightInPoints = heightInCm * POINTS_PER_CM;
        }

        return PdfGenerator.generatePdf(pageWidthInPoints, pageHeightInPoints,
                                      allPagesContent, "Amiri-Regular.ttf", templateImagePath);
    }

    @FXML
    private void generateMultipleChecks() {
        if (!validateInput()) return;

        try {
            int numChecks = Integer.parseInt(numChecksField.getText());
            String interval = intervalComboBox.getValue();
            
            LocalDate currentDate = firstCheckDatePicker.getValue();
            multiChequeDataList.clear();
            
            for (int i = 0; i < numChecks; i++) {
                ChequeData chequeData = new ChequeData();
                chequeData.setDate(currentDate.toString());
                chequeData.setBeneficiaryName(beneficiaryField.getText());
                chequeData.setAmountNumeric(amountField.getText());
                chequeData.setAmountWords(amountWordsField.getText());
                chequeData.setSignerName(signerField.getText());
                
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
            
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number of checks.");
        } catch (Exception e) {
            showAlert("Error", "Failed to generate cheques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void clearMultiChequeTable() {
        multiChequeDataList.clear();
    }

    @FXML
    private void printAllCheques() {
        if (multiChequeDataList.isEmpty()) {
            showAlert("No Cheques", "Please generate cheques first before printing.");
            return;
        }

        try {
            // Use the same logic as printAndSaveMultipleChecks but with cheques from table
            java.util.List<java.util.List<PdfContent>> allPagesContent = new java.util.ArrayList<>();
            
            // Create PDF content for each cheque from the table
            for (ChequeData chequeData : multiChequeDataList) {
                java.util.List<PdfContent> pageContent = createPdfContentForCheque(chequeData);
                allPagesContent.add(pageContent);
            }
            
            // --- CONVERSION ---
            // 1 inch = 72 points. 1 inch = 2.54 cm.
            // Points per cm = 72 / 2.54 = 28.346
            final float POINTS_PER_CM = 72f / 2.54f;

            // Get selected template for dimensions and background image
            BankTemplate.Template selectedTemplate = templateComboBox.getValue();
            float pageWidthInPoints, pageHeightInPoints;
            String templateImagePath = null;
            
            if (selectedTemplate != null) {
                // --- DESIRED DIMENSIONS (from bank.json in cm) ---
                float widthInCm = selectedTemplate.getWidth();
                float heightInCm = selectedTemplate.getHeight();
                
                // --- CALCULATED DIMENSIONS IN POINTS ---
                pageWidthInPoints = widthInCm * POINTS_PER_CM;
                pageHeightInPoints = heightInCm * POINTS_PER_CM;
                
                templateImagePath = selectedTemplate.getImagePath();
                System.out.println("=== MULTIPLE CHEQUES FROM TABLE DIMENSIONS DEBUG ===");
                System.out.println("Template: " + selectedTemplate.getTemplateName());
                System.out.println("Width from bank.json: " + widthInCm + " cm");
                System.out.println("Height from bank.json: " + heightInCm + " cm");
                System.out.println("POINTS_PER_CM: " + POINTS_PER_CM);
                System.out.println("Calculated width in points: " + pageWidthInPoints);
                System.out.println("Calculated height in points: " + pageHeightInPoints);
                System.out.println("Template image: " + templateImagePath);
                System.out.println("Number of cheques from table: " + multiChequeDataList.size());
                System.out.println("=========================================");
            } else {
                // Fallback to default dimensions
                float widthInCm = 16.7f;
                float heightInCm = 8.1f;
                pageWidthInPoints = widthInCm * POINTS_PER_CM;
                pageHeightInPoints = heightInCm * POINTS_PER_CM;
                System.out.println("Using default dimensions for multiple cheques from table: " + widthInCm + "cm x " + heightInCm + "cm");
                System.out.println("Converted to points: " + pageWidthInPoints + " x " + pageHeightInPoints);
                System.out.println("Number of cheques from table: " + multiChequeDataList.size());
            }
            
            // Generate PDF document using the same method as printAndSaveMultipleChecks
            PDDocument document = PdfGenerator.generatePdf(pageWidthInPoints, pageHeightInPoints,
                                                         allPagesContent, "Amiri-Regular.ttf", templateImagePath);
            
            // Print the PDF
            org.chequePrinter.service.PdfPrinter.printPdf(document, selectedTemplate.getWidth(), selectedTemplate.getHeight());
            
            // Save all cheques from table to database
            for (ChequeData cheque : multiChequeDataList) {
                DatabaseService.saveCheque(cheque);
            }
            
            loadChequeRecords();
            showAlert("Success", multiChequeDataList.size() + " cheques from table printed and saved successfully!");
            
        } catch (Exception e) {
            showAlert("Error", "Failed to print cheques from table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}