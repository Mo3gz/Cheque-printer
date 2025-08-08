package org.chequePrinter.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import org.chequePrinter.model.BankTemplate;
import org.chequePrinter.util.JsonLoader;

import java.util.List;

public class BankTemplateController {

    @FXML
    private ComboBox<BankTemplate> bankComboBox;
    @FXML
    private ComboBox<BankTemplate.Template> templateComboBox;

    private List<BankTemplate> bankTemplates;
    private ChequePreviewController previewController;

    public void initialize() {
        loadBankConfiguration();
        setupUI();
    }

    private void loadBankConfiguration() {
        try {
            bankTemplates = JsonLoader.loadBankTemplates("/bank.json");
            System.out.println("Bank configuration loaded successfully: " + bankTemplates.size() + " banks");
        } catch (Exception e) {
            System.err.println("Failed to load bank configuration: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load bank configuration: " + e.getMessage());
        }
    }

    private void setupUI() {
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
            if (selectedTemplate != null && previewController != null) {
                previewController.setSelectedTemplate(selectedTemplate);
            }
        });
    }

    private void updateTemplateComboBox(BankTemplate bank) {
        if (bank != null && bank.getTemplates() != null) {
            templateComboBox.setItems(FXCollections.observableArrayList(bank.getTemplates()));
            if (!bank.getTemplates().isEmpty()) {
                templateComboBox.setValue(bank.getTemplates().get(0));
                if (previewController != null) {
                    previewController.loadTemplateImageForBank(bank.getTemplates().get(0));
                }
            }
        }
    }

    public BankTemplate getSelectedBank() {
        return bankComboBox.getValue();
    }

    public BankTemplate.Template getSelectedTemplate() {
        return templateComboBox.getValue();
    }

    public void setSelectedBank(BankTemplate bank) {
        bankComboBox.setValue(bank);
        updateTemplateComboBox(bank);
    }

    public void setSelectedTemplate(BankTemplate.Template template) {
        templateComboBox.setValue(template);
        if (previewController != null) {
            previewController.setSelectedTemplate(template);
        }
    }

    public List<BankTemplate> getAllBankTemplates() {
        return bankTemplates;
    }

    public BankTemplate getBankByName(String bankName) {
        if (bankTemplates != null) {
            return bankTemplates.stream()
                    .filter(bank -> bank.getName().equalsIgnoreCase(bankName))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public BankTemplate.Template getTemplateByName(String bankName, String templateName) {
        BankTemplate bank = getBankByName(bankName);
        if (bank != null && bank.getTemplates() != null) {
            return bank.getTemplates().stream()
                    .filter(template -> template.getTemplateName().equalsIgnoreCase(templateName))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public void refreshBankConfiguration() {
        loadBankConfiguration();
        setupUI();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Dependency injection
    public void setPreviewController(ChequePreviewController previewController) {
        this.previewController = previewController;
    }

    // Getters for UI components
    public ComboBox<BankTemplate> getBankComboBox() { return bankComboBox; }
    public ComboBox<BankTemplate.Template> getTemplateComboBox() { return templateComboBox; }
}