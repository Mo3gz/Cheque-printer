package org.chequePrinter.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.chequePrinter.model.ChequeData;
import org.chequePrinter.util.ArabicNumberToWords;

import java.time.LocalDate;

public class ChequeFormController {

    @FXML
    private DatePicker firstCheckDatePicker;
    @FXML
    private TextField beneficiaryField;
    @FXML
    private TextField amountField;
    @FXML
    private TextField amountWordsField;
    @FXML
    private TextField signerField;
    @FXML
    private TextField numChecksField;
    @FXML
    private ComboBox<String> intervalComboBox;

    public void initialize() {
        setupUI();
        setupBindings();
    }

    private void setupUI() {
        intervalComboBox.getItems().addAll("1 Month", "2 Months", "3 Months", "4 Months", "5 Months", "6 Months",
                                          "7 Months", "8 Months", "9 Months", "10 Months", "11 Months", "12 Months");
        intervalComboBox.setValue("1 Month");
        firstCheckDatePicker.setValue(LocalDate.now());
        numChecksField.setText("1");
    }

    private void setupBindings() {
        // Update amount words when amount changes
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateAmountWords();
        });
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

    public ChequeData createChequeData() {
        ChequeData chequeData = new ChequeData();
        chequeData.setDate(firstCheckDatePicker.getValue().toString());
        chequeData.setBeneficiaryName(beneficiaryField.getText());
        chequeData.setAmountNumeric(amountField.getText());
        chequeData.setAmountWords(amountWordsField.getText());
        chequeData.setSignerName(signerField.getText());
        return chequeData;
    }

    public void populateForm(ChequeData chequeData) {
        try {
            firstCheckDatePicker.setValue(LocalDate.parse(chequeData.getDate()));
        } catch (Exception e) {
            firstCheckDatePicker.setValue(LocalDate.now());
        }
        beneficiaryField.setText(chequeData.getBeneficiaryName());
        amountField.setText(chequeData.getAmountNumeric());
        amountWordsField.setText(chequeData.getAmountWords());
        signerField.setText(chequeData.getSignerName());
    }

    public boolean validateInput() {
        // Validate date
        if (firstCheckDatePicker.getValue() == null) {
            showAlert("Validation Error", "Please select a valid date.");
            return false;
        }
        
        // Validate beneficiary name (Arabic or English only, no numbers or special characters)
        String beneficiary = beneficiaryField.getText().trim();
        if (beneficiary.isEmpty()) {
            showAlert("Validation Error", "Please enter beneficiary name.");
            return false;
        }
        
        // Regular expression for Arabic and English letters only (with spaces)
        String nameRegex = "^[\\u0600-\\u06FF\\u0750-\\u077F\\sA-Za-z]+";
        if (!beneficiary.matches(nameRegex)) {
            showAlert("Validation Error", "Beneficiary name can only contain Arabic or English letters and spaces.");
            return false;
        }
        
        // Validate amount is a positive whole number
        try {
            String amountStr = amountField.getText().trim();
            double amount = Double.parseDouble(amountStr);
            
            if (amount <= 0) {
                showAlert("Validation Error", "Amount must be a positive number.");
                return false;
            }
            
            // Check if the input has a decimal point and non-zero decimal part
            if (amountStr.contains(".") && !amountStr.endsWith(".0") && !amountStr.endsWith(".00")) {
                showAlert("Validation Error", "Amount must be a whole number (e.g., 5 or 5.0).");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid numeric amount.");
            return false;
        }
        
        // Validate signer's name (Arabic or English only, no numbers or special characters)
        String signerName = signerField.getText().trim();
        if (signerName.isEmpty()) {
            showAlert("Validation Error", "Please enter signer's name.");
            return false;
        }
        
        if (!signerName.matches(nameRegex)) {
            showAlert("Validation Error", "Signer's name can only contain Arabic or English letters and spaces.");
            return false;
        }
        
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Getters for form fields
    public DatePicker getFirstCheckDatePicker() { return firstCheckDatePicker; }
    public TextField getBeneficiaryField() { return beneficiaryField; }
    public TextField getAmountField() { return amountField; }
    public TextField getAmountWordsField() { return amountWordsField; }
    public TextField getSignerField() { return signerField; }
    public TextField getNumChecksField() { return numChecksField; }
    public ComboBox<String> getIntervalComboBox() { return intervalComboBox; }
}