package org.chequePrinter.service;

import com.lowagie.text.Element;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.chequePrinter.model.ChequeData;
import org.chequePrinter.model.PdfContent;
import org.chequePrinter.model.BankTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfService {

    public static PDDocument createPdf(BankTemplate.Template bankTemplate, List<ChequeData> chequeDataList, String interval) throws IOException {
        BankTemplate.Template template = bankTemplate;
        List<List<PdfContent>> allPagesContent = new ArrayList<>();

        for (ChequeData chequeData : chequeDataList) {
            List<PdfContent> pageContent = new ArrayList<>();
            pageContent.add(new PdfContent(chequeData.getDate(), template.getFields().get("dateField").getFontSize(), Element.ALIGN_CENTER, template.getFields().get("dateField").getX(), template.getFields().get("dateField").getY(), template.getFields().get("dateField").getWidth(), 0));
            pageContent.add(new PdfContent(chequeData.getBeneficiaryName(), template.getFields().get("beneficiaryField").getFontSize(), Element.ALIGN_CENTER, template.getFields().get("beneficiaryField").getX(), template.getFields().get("beneficiaryField").getY(), template.getFields().get("beneficiaryField").getWidth(), 0));
            pageContent.add(new PdfContent(chequeData.getAmountWords(), template.getFields().get("amountWordsField").getFontSize(), Element.ALIGN_CENTER, template.getFields().get("amountWordsField").getX(), template.getFields().get("amountWordsField").getY(), template.getFields().get("amountWordsField").getWidth(), 0));
            pageContent.add(new PdfContent(chequeData.getAmountNumeric(), template.getFields().get("amountField").getFontSize(), Element.ALIGN_CENTER, template.getFields().get("amountField").getX(), template.getFields().get("amountField").getY(), template.getFields().get("amountField").getWidth(), 0));
            pageContent.add(new PdfContent(chequeData.getSignerName(), template.getFields().get("signerField").getFontSize(), Element.ALIGN_CENTER, template.getFields().get("signerField").getX(), template.getFields().get("signerField").getY(), template.getFields().get("signerField").getWidth(), 0));
            // Add fixed text field if present
            BankTemplate.FixedTextField fixedText = template.getFixedTextField();
            if (fixedText != null && fixedText.getText() != null && !fixedText.getText().trim().isEmpty()) {
                pageContent.add(new PdfContent(
                    fixedText.getText(),
                    fixedText.getFontSize(),
                    fixedText.getAlignment(),
                    fixedText.getX(),
                    fixedText.getY(),
                    fixedText.getWidth(),
                    0));
            }
            allPagesContent.add(pageContent);
        }

        // Convert template dimensions from centimeters to points
        // 1 inch = 72 points. 1 inch = 2.54 cm.
        // Points per cm = 72 / 2.54 = 28.346
        final float POINTS_PER_CM = 72f / 2.54f;
        
        float widthInCm = template.getWidth();
        float heightInCm = template.getHeight();
        float pageWidthInPoints = widthInCm * POINTS_PER_CM;
        float pageHeightInPoints = heightInCm * POINTS_PER_CM;
        
        System.out.println("=== PDFSERVICE DIMENSIONS DEBUG ===");
        System.out.println("Template dimensions from bank.json: " + widthInCm + " x " + heightInCm + " cm");
        System.out.println("Converted to points: " + pageWidthInPoints + " x " + pageHeightInPoints);
        System.out.println("===================================");
        
        return PdfGenerator.generatePdf(pageWidthInPoints, pageHeightInPoints, allPagesContent, "Amiri-Regular.ttf");
    }
}
