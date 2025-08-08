package org.chequePrinter.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.chequePrinter.model.BankTemplate;
import org.chequePrinter.util.JsonLoader;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChequePreviewController {

    @FXML
    private Pane previewPane;
    @FXML
    private ImageView chequeImageView;
    @FXML
    private Text dateText;
    @FXML
    private Text beneficiaryText;
    @FXML
    private Text amountWordsText;
    @FXML
    private Text amountText;
    @FXML
    private Text signerText;

    private BankTemplate bankTemplate;
    private BankTemplate.Template selectedTemplate;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void initialize() {
        loadBankTemplate();
        setupUI();
    }

    private void loadBankTemplate() {
        try {
            List<BankTemplate> bankTemplates = JsonLoader.loadBankTemplates("/bank.json");
            if (bankTemplates != null && !bankTemplates.isEmpty()) {
                bankTemplate = bankTemplates.get(0); // Use the first bank template (NBE)
                selectedTemplate = bankTemplate.getTemplates().get(0);
            } else {
                throw new IOException("No bank templates found in bank.json");
            }
            loadTemplateImage();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load bank template configuration (bank.json).");
        }
    }

    private void loadTemplateImage() {
        if (selectedTemplate != null) {
            String imagePath = selectedTemplate.getImagePath();
            if (imagePath == null || getClass().getResourceAsStream(imagePath) == null) {
                showAlert("Error", "Cheque image not found at: " + imagePath + ".\nPlease make sure the image exists.");
                previewPane.setStyle("-fx-background-color: lightgray;");
                return;
            }
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            if (image.isError()) {
                showAlert("Error", "Failed to load cheque image. It may be corrupt or in an unsupported format.");
                previewPane.setStyle("-fx-background-color: lightgray;");
            } else {
                chequeImageView.setImage(image);
            }
        }
    }

    public void loadTemplateImageForBank(BankTemplate.Template template) {
        try {
            if (template != null && template.getImagePath() != null) {
                InputStream imageStream = getClass().getResourceAsStream(template.getImagePath());
                if (imageStream != null) {
                    Image image = new Image(imageStream);
                    chequeImageView.setImage(image);
                    imageStream.close();
                    selectedTemplate = template;
                    System.out.println("Loaded template image: " + template.getImagePath());
                } else {
                    System.err.println("Template image not found: " + template.getImagePath());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading template image: " + e.getMessage());
        }
    }

    private void setupUI() {
        if (chequeImageView != null && previewPane != null) {
            chequeImageView.fitWidthProperty().bind(previewPane.widthProperty());
            chequeImageView.fitHeightProperty().bind(previewPane.heightProperty());

            previewPane.widthProperty().addListener((obs, oldVal, newVal) -> updatePreview());
            previewPane.heightProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        }
    }

    public void updatePreview() {
        if (selectedTemplate == null || previewPane == null) return;
        
        // Add null checks for all text components
        if (dateText == null || beneficiaryText == null || amountWordsText == null ||
            amountText == null || signerText == null) {
            // Preview components not properly injected, skip preview update silently
            return;
        }

        double scaleX = previewPane.getWidth() / selectedTemplate.getWidth();
        double scaleY = previewPane.getHeight() / selectedTemplate.getHeight();

        updateText(dateText, dateText.getText(), selectedTemplate.getFields().get("dateField"), scaleX, scaleY);
        updateText(beneficiaryText, beneficiaryText.getText(), selectedTemplate.getFields().get("beneficiaryField"), scaleX, scaleY);
        updateText(amountWordsText, amountWordsText.getText(), selectedTemplate.getFields().get("amountWordsField"), scaleX, scaleY);
        updateText(amountText, amountText.getText(), selectedTemplate.getFields().get("amountField"), scaleX, scaleY);
        updateText(signerText, signerText.getText(), selectedTemplate.getFields().get("signerField"), scaleX, scaleY);
    }

    public void updatePreviewWithData(String date, String beneficiary, String amountWords, String amount, String signer) {
        // Add null checks to prevent NullPointerException
        if (dateText == null || beneficiaryText == null || amountWordsText == null ||
            amountText == null || signerText == null) {
            // Preview components not properly injected, skip preview update silently
            return;
        }

        // Format date according to template's dateFormat
        String formattedDate = date;
        if (selectedTemplate != null && selectedTemplate.getDateFormat() != null && date != null) {
            try {
                LocalDate parsedDate = LocalDate.parse(date);
                switch (selectedTemplate.getDateFormat()) {
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
                        formattedDate = parsedDate.format(dateFormatter);
                        break;
                }
            } catch (Exception e) {
                // Use original date if parsing fails
            }
        }

        dateText.setText(formattedDate != null ? formattedDate : "");
        beneficiaryText.setText(beneficiary != null ? beneficiary : "");
        amountWordsText.setText(amountWords != null ? amountWords : "");
        amountText.setText(amount != null ? amount : "");
        signerText.setText(signer != null ? signer : "");

        updatePreview();
    }

    private void updateText(Text textNode, String text, BankTemplate.Field field, double scaleX, double scaleY) {
        if (field == null || textNode == null) return;
        
        try {
            textNode.setText(text != null ? text : "");
            textNode.setX(field.getX() * scaleX);
            textNode.setY(field.getY() * scaleY);
            textNode.setStyle(String.format("-fx-font-size: %fpx;", field.getFontSize() * Math.min(scaleX, scaleY)));
        } catch (Exception e) {
            // Silently handle any text update errors
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Getters
    public BankTemplate.Template getSelectedTemplate() { return selectedTemplate; }
    public void setSelectedTemplate(BankTemplate.Template template) { 
        this.selectedTemplate = template;
        loadTemplateImageForBank(template);
    }
    public Pane getPreviewPane() { return previewPane; }
    public ImageView getChequeImageView() { return chequeImageView; }
}