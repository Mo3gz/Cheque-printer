package org.chequePrinter.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.chequePrinter.model.BankTemplate;
import org.chequePrinter.model.ChequeData;

import java.util.List;

public class ChequeControllerRefactored {

    // Injected sub-controllers
    private ChequeFormController formController;
    private ChequePreviewController previewController;
    private ChequePrintController printController;
    private ChequeDataController dataController;
    private ChequeFilterController filterController;

    @FXML
    public void initialize() {
        // Initialize sub-controllers
        formController = new ChequeFormController();
        previewController = new ChequePreviewController();
        printController = new ChequePrintController();
        dataController = new ChequeDataController();
        filterController = new ChequeFilterController();

        // Initialize all sub-controllers
        formController.initialize();
        previewController.initialize();
        dataController.initialize();
        filterController.initialize();

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
    }

    private void setupEventHandlers() {
        // Set up form field listeners to update preview
        formController.getFirstCheckDatePicker().valueProperty().addListener((obs, old, val) -> updatePreview());
        formController.getBeneficiaryField().textProperty().addListener((obs, old, val) -> updatePreview());
        formController.getAmountField().textProperty().addListener((obs, old, val) -> updatePreview());
        formController.getAmountWordsField().textProperty().addListener((obs, old, val) -> updatePreview());
        formController.getSignerField().textProperty().addListener((obs, old, val) -> updatePreview());
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
        if (!formController.validateInput()) {
            return;
        }

        try {
            ChequeData chequeData = formController.createChequeData();
            BankTemplate.Template selectedTemplate = previewController.getSelectedTemplate();

            if (selectedTemplate == null) {
                showAlert("Error", "Please select a valid bank template before printing.");
                return;
            }

            // Retrieve template dimensions
            float widthInCm = selectedTemplate.getWidth();
            float heightInCm = selectedTemplate.getHeight();

            PDDocument document = printController.generateChequePDF(chequeData, selectedTemplate);
            printController.printPDF(document, widthInCm, heightInCm);

            // Save record to the database
            dataController.saveCheque(chequeData);

            showAlert("Success", "Cheque printed and record saved successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to print or save cheque.");
        }
    }

    @FXML
    private void printAndSaveMultipleChecks() {
        if (!formController.validateInput()) {
            return;
        }

        try {
            int numChecks = Integer.parseInt(formController.getNumChecksField().getText());
            String interval = formController.getIntervalComboBox().getValue();

            ChequeData baseData = formController.createChequeData();
            BankTemplate.Template selectedTemplate = previewController.getSelectedTemplate();

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

            showAlert("Success", "Cheques printed and records saved successfully.");
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number of checks.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to print or save cheques.");
        }
    }

    @FXML
    private void applyDateFilter() {
        filterController.applyDateFilter();
    }

    @FXML
    private void deleteSelectedRecord() {
        dataController.deleteSelectedRecord();
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

    // Setters for dependency injection
    public void setFormController(ChequeFormController formController) { this.formController = formController; }
    public void setPreviewController(ChequePreviewController previewController) { this.previewController = previewController; }
    public void setPrintController(ChequePrintController printController) { this.printController = printController; }
    public void setDataController(ChequeDataController dataController) { this.dataController = dataController; }
    public void setFilterController(ChequeFilterController filterController) { this.filterController = filterController; }
}