package org.chequePrinter.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.chequePrinter.model.BankTemplate;
import org.chequePrinter.model.ChequeData;
import org.chequePrinter.service.PaymentPlanService;
import org.chequePrinter.service.PdfPrinter;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SimpleController implements Initializable {

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
        // Initialize sub-controllers
        initializeSubControllers();
        setupControllerDependencies();
        setupEventHandlers();
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
        if (!formController.validateInput()) return;

        try {
            ChequeData chequeData = formController.createChequeData();
            BankTemplate.Template selectedTemplate = templateController.getSelectedTemplate();

            if (selectedTemplate == null) {
                showAlert("Error", "Please select a valid bank template before printing.");
                return;
            }

            float widthInCm = selectedTemplate.getWidth();
            float heightInCm = selectedTemplate.getHeight();

            // Generate PDF and print
            PDDocument document = printController.generateChequePDF(chequeData, selectedTemplate);
            printController.printPDF(document, widthInCm, heightInCm);
            
            // Save to database
            dataController.saveCheque(chequeData);
            
            showAlert("Success", "Cheque printed and saved successfully!");
            
        } catch (Exception e) {
            showAlert("Error", "Failed to print cheque: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void printAndSaveMultipleChecks() {
        if (!formController.validateInput()) return;

        try {
            int numChecks = Integer.parseInt(formController.getNumChecksField().getText());
            String interval = formController.getIntervalComboBox().getValue();
            
            ChequeData baseData = formController.createChequeData();
            BankTemplate.Template selectedTemplate = templateController.getSelectedTemplate();

            if (selectedTemplate == null) {
                showAlert("Error", "Please select a valid bank template before printing.");
                return;
            }

            // Generate cheque data list
            List<ChequeData> chequesToPrint = printController.generateChequeDataList(baseData, numChecks, interval);

            // Create and print PDF
            PDDocument document = printController.generateMultipleChequePDF(chequesToPrint, selectedTemplate);
            printController.printPDF(document, selectedTemplate.getWidth(), selectedTemplate.getHeight());

            // Save records to the database
            dataController.saveCheques(chequesToPrint);
            
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
        if (!formController.validateInput()) return;

        try {
            int numChecks = Integer.parseInt(formController.getNumChecksField().getText());
            String interval = formController.getIntervalComboBox().getValue();
            
            ChequeData baseData = formController.createChequeData();
            dataController.generateMultipleChecks(baseData, numChecks, interval);
            
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number of checks.");
        } catch (Exception e) {
            showAlert("Error", "Failed to generate cheques: " + e.getMessage());
            e.printStackTrace();
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
        try {
            // Get cheques from the multiple cheque editing table
            List<ChequeData> cheques = new ArrayList<>(dataController.getMultiChequeData());
            
            if (cheques.isEmpty()) {
                showAlert("No Data", "No cheques found in the editing table. Please generate multiple cheques first.");
                return;
            }
            
            // Get signer name from the first cheque (assuming all cheques have the same signer)
            String signerName = cheques.get(0).getSignerName();
            
            // Generate payment plan PDF
            PDDocument document = PaymentPlanService.generatePaymentPlanPDF(cheques, signerName);
            
            // Print the PDF using dedicated A4 Portrait method for payment plan
            PdfPrinter.printPaymentPlanPdf(document);
            
            showAlert("Success", "Payment plan printed successfully! Total cheques: " + cheques.size());
            
        } catch (Exception e) {
            showAlert("Error", "Failed to print payment plan: " + e.getMessage());
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

    // Getters for sub-controllers (for testing or external access)
    public ChequeFormController getFormController() { return formController; }
    public ChequePreviewController getPreviewController() { return previewController; }
    public ChequePrintController getPrintController() { return printController; }
    public ChequeDataController getDataController() { return dataController; }
    public ChequeFilterController getFilterController() { return filterController; }
    public ChequeExportController getExportController() { return exportController; }
    public BankTemplateController getTemplateController() { return templateController; }
}