package org.chequePrinter.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.chequePrinter.model.BankTemplate;
import org.chequePrinter.model.ChequeData;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SimpleControllerRefactored implements Initializable {

    // Injected sub-controllers
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
        formController = new ChequeFormController();
        previewController = new ChequePreviewController();
        printController = new ChequePrintController();
        dataController = new ChequeDataController();
        filterController = new ChequeFilterController();
        exportController = new ChequeExportController();
        templateController = new BankTemplateController();

        // Initialize all sub-controllers
        formController.initialize();
        previewController.initialize();
        dataController.initialize();
        filterController.initialize();
        exportController.initialize();
        templateController.initialize();

        // Set up dependencies between controllers
        setupControllerDependencies();
        setupEventHandlers();
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Getters for sub-controllers (for FXML injection or manual setup)
    public ChequeFormController getFormController() { return formController; }
    public ChequePreviewController getPreviewController() { return previewController; }
    public ChequePrintController getPrintController() { return printController; }
    public ChequeDataController getDataController() { return dataController; }
    public ChequeFilterController getFilterController() { return filterController; }
    public ChequeExportController getExportController() { return exportController; }
    public BankTemplateController getTemplateController() { return templateController; }

    // Setters for dependency injection
    public void setFormController(ChequeFormController formController) { this.formController = formController; }
    public void setPreviewController(ChequePreviewController previewController) { this.previewController = previewController; }
    public void setPrintController(ChequePrintController printController) { this.printController = printController; }
    public void setDataController(ChequeDataController dataController) { this.dataController = dataController; }
    public void setFilterController(ChequeFilterController filterController) { this.filterController = filterController; }
    public void setExportController(ChequeExportController exportController) { this.exportController = exportController; }
    public void setTemplateController(BankTemplateController templateController) { this.templateController = templateController; }
}