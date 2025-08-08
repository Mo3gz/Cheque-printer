package org.chequePrinter.controller;

import javafx.scene.control.Alert;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.chequePrinter.model.BankTemplate;
import org.chequePrinter.model.ChequeData;
import org.chequePrinter.model.PdfContent;
import org.chequePrinter.service.PdfGenerator;
import org.chequePrinter.service.PdfService;

import com.lowagie.text.Element;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChequePrintController {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dbDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PDDocument createSingleChequePDF(ChequeData chequeData, BankTemplate.Template template) throws Exception {
        List<ChequeData> chequeList = new ArrayList<>();
        chequeList.add(chequeData);
        return PdfService.createPdf(template, chequeList, null);
    }

    public PDDocument createMultipleChequePDF(List<ChequeData> chequeDataList, BankTemplate.Template template, String interval) throws Exception {
        return PdfService.createPdf(template, chequeDataList, interval);
    }

    public PDDocument generateChequePDF(ChequeData chequeData, BankTemplate.Template selectedTemplate) throws Exception {
        List<PdfContent> contentList = createPdfContentForCheque(chequeData, selectedTemplate);
        List<List<PdfContent>> allPagesContent = new ArrayList<>();
        allPagesContent.add(contentList);

        // Conversion: 1 inch = 72 points, 1 inch = 2.54 cm
        final float POINTS_PER_CM = 72f / 2.54f;

        float pageWidthInPoints, pageHeightInPoints;
        String templateImagePath = null;
        
        if (selectedTemplate != null) {
            float widthInCm = selectedTemplate.getWidth();
            float heightInCm = selectedTemplate.getHeight();
            
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

    public PDDocument generateMultipleChequePDF(List<ChequeData> chequeDataList, BankTemplate.Template selectedTemplate) throws Exception {
        List<List<PdfContent>> allPagesContent = new ArrayList<>();
        
        for (ChequeData chequeData : chequeDataList) {
            List<PdfContent> pageContent = createPdfContentForCheque(chequeData, selectedTemplate);
            allPagesContent.add(pageContent);
        }
        
        // Conversion: 1 inch = 72 points, 1 inch = 2.54 cm
        final float POINTS_PER_CM = 72f / 2.54f;

        float pageWidthInPoints, pageHeightInPoints;
        String templateImagePath = null;
        
        if (selectedTemplate != null) {
            float widthInCm = selectedTemplate.getWidth();
            float heightInCm = selectedTemplate.getHeight();
            
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
        
        return PdfGenerator.generatePdf(pageWidthInPoints, pageHeightInPoints,
                                      allPagesContent, "Amiri-Regular.ttf", templateImagePath);
    }

    public void printPDF(PDDocument document, float widthInCm, float heightInCm) throws Exception {
        org.chequePrinter.service.PdfPrinter.printPdf(document, widthInCm, heightInCm);
    }

    public List<ChequeData> generateChequeDataList(ChequeData baseData, int numChecks, String interval) {
        List<ChequeData> chequeDataList = new ArrayList<>();
        LocalDate currentDate = parseDate(baseData.getDate());
        
        for (int i = 0; i < numChecks; i++) {
            ChequeData cheque = new ChequeData(
                    currentDate.format(dateFormatter),
                    baseData.getBeneficiaryName(),
                    baseData.getAmountNumeric(),
                    baseData.getAmountWords(),
                    baseData.getSignerName()
            );
            chequeDataList.add(cheque);
            currentDate = getNextDate(currentDate, interval);
        }
        return chequeDataList;
    }

    private LocalDate getNextDate(LocalDate date, String interval) {
        if (interval == null) {
            return date;
        }
        
        if (interval.contains("Month")) {
            String monthsStr = interval.split(" ")[0];
            try {
                int months = Integer.parseInt(monthsStr);
                return date.plusMonths(months);
            } catch (NumberFormatException e) {
                return date.plusMonths(1);
            }
        }
        
        switch (interval) {
            case "1 Month":
                return date.plusMonths(1);
            case "3 Months":
                return date.plusMonths(3);
            case "6 Months":
                return date.plusMonths(6);
            case "12 Months":
                return date.plusYears(1);
            default:
                return date;
        }
    }

    private List<PdfContent> createPdfContentForCheque(ChequeData chequeData, BankTemplate.Template selectedTemplate) {
        List<PdfContent> contentList = new ArrayList<>();

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
                String formattedDate = formatDateForTemplate(chequeData.getDate(), selectedTemplate.getDateFormat());
                contentList.add(new PdfContent(formattedDate, datePos.getFontSize(), datePos.getAlignment(),
                    datePos.getX(), datePos.getY(), datePos.getWidth(), 23f));
                System.out.println("Date field: '" + chequeData.getDate() + "' at (" + datePos.getX() + ", " + datePos.getY() + ")");
            }
            
            BankTemplate.Field beneficiaryPos = selectedTemplate.getFields().get("beneficiaryField");
            if (beneficiaryPos != null) {
                String beneficiaryText = chequeData.getBeneficiaryName();
                contentList.add(new PdfContent(beneficiaryText, beneficiaryPos.getFontSize(), beneficiaryPos.getAlignment(),
                    beneficiaryPos.getX(), beneficiaryPos.getY(), beneficiaryPos.getWidth(), 30f));
                System.out.println("Beneficiary field: '" + beneficiaryText + "' at (" + beneficiaryPos.getX() + ", " + beneficiaryPos.getY() + ")");
            }
            
            BankTemplate.Field amountWordsPos = selectedTemplate.getFields().get("amountWordsField");
            if (amountWordsPos != null) {
                String amountWordsText = chequeData.getAmountWords();
                contentList.add(new PdfContent(amountWordsText, amountWordsPos.getFontSize(), amountWordsPos.getAlignment(),
                    amountWordsPos.getX(), amountWordsPos.getY(), amountWordsPos.getWidth(), 30f));
                System.out.println("Amount words field: '" + amountWordsText + "' at (" + amountWordsPos.getX() + ", " + amountWordsPos.getY() + ")");
            }
            
            BankTemplate.Field amountNumericPos = selectedTemplate.getFields().get("amountField");
            if (amountNumericPos != null) {
                String amountNumericText = chequeData.getAmountNumeric();
                contentList.add(new PdfContent(amountNumericText, amountNumericPos.getFontSize(), amountNumericPos.getAlignment(),
                    amountNumericPos.getX(), amountNumericPos.getY(), amountNumericPos.getWidth(), 23f));
                System.out.println("Amount numeric field: '" + amountNumericText + "' at (" + amountNumericPos.getX() + ", " + amountNumericPos.getY() + ")");
            }
            
            BankTemplate.Field signerPos = selectedTemplate.getFields().get("signerField");
            if (signerPos != null) {
                String signerText = chequeData.getSignerName();
                contentList.add(new PdfContent(signerText, signerPos.getFontSize(), signerPos.getAlignment(),
                    signerPos.getX(), signerPos.getY(), signerPos.getWidth(), 23f));
                System.out.println("Signer field: '" + signerText + "' at (" + signerPos.getX() + ", " + signerPos.getY() + ")");
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

    private String formatDateForTemplate(String dateText, String templateDateFormat) {
        String formattedDate = dateText;
        if (templateDateFormat != null) {
            try {
                LocalDate parsedDate = parseDate(dateText);
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
                        formattedDate = parsedDate.format(dateFormatter);
                        break;
                }
            } catch (Exception e) {
                // fallback to raw dateText
            }
        }
        return formattedDate;
    }

    private LocalDate parseDate(String dateText) {
        if (dateText == null || dateText.trim().isEmpty()) {
            return LocalDate.now();
        }
        
        try {
            // Try parsing with ISO format first (yyyy-MM-dd)
            if (dateText.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(dateText);
            }
            
            // Try parsing with dd/MM/yyyy format
            if (dateText.matches("\\d{2}/\\d{2}/\\d{4}")) {
                return LocalDate.parse(dateText, dateFormatter);
            }
            
            // Try parsing with yyyy/MM/dd format
            if (dateText.matches("\\d{4}/\\d{2}/\\d{2}")) {
                return LocalDate.parse(dateText.replace('/', '-'));
            }
            
            // Try parsing with dd-MM-yyyy format
            if (dateText.matches("\\d{2}-\\d{2}-\\d{4}")) {
                String[] parts = dateText.split("-");
                return LocalDate.of(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
            }
            
            // Try parsing with yyyy-MM-dd format (database format)
            if (dateText.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(dateText, dbDateFormatter);
            }
            
            // Default fallback - try ISO format
            return LocalDate.parse(dateText);
            
        } catch (Exception e) {
            System.err.println("Failed to parse date: " + dateText + ", using current date instead");
            return LocalDate.now();
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