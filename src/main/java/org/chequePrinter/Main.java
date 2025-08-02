package org.chequePrinter;

import org.chequePrinter.model.BankTemplate;
import org.chequePrinter.model.ChequeData;
import org.chequePrinter.util.JsonLoader;
import org.chequePrinter.service.PdfPrinter;
import org.chequePrinter.service.PdfService;
import org.chequePrinter.util.ArabicNumberToWords;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.awt.print.PrinterException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws PrinterException {
        System.out.println("Starting DebugMain...");

        // 1. Load Bank Templates
        List<BankTemplate> bankTemplates = null;
        try {
            bankTemplates = JsonLoader.loadBankTemplates("/bank.json");
            System.out.println("Bank templates loaded successfully.");
        } catch (IOException e) {
            System.err.println("Error loading bank templates: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // 2. Find QNB Bank Template
        BankTemplate qnbBankTemplate = null;
        if (bankTemplates != null) {
            for (BankTemplate bt : bankTemplates) {
                if (bt.getName().equals("بنك قطر الوطني (QNB)")) {
                    qnbBankTemplate = bt;
                    break;
                }
            }
        }

        if (qnbBankTemplate == null) {
            System.err.println("QNB Bank Template not found in bank.json.");
            return;
        }
        System.out.println("Found QNB Bank Template: " + qnbBankTemplate.getName());
        System.out.println("QNB Template Name: " + qnbBankTemplate.getTemplates().get(0).getTemplateName());

        // 3. Create Sample Cheque Data
        String amountNumeric = "12345.67";
        String amountWords = ArabicNumberToWords.convert(Double.parseDouble(amountNumeric));

        ChequeData sampleCheque = new ChequeData(
                "08/02/2025",
                "المستفيد التجريبي",
                amountNumeric,
                amountWords + " فقط لا غير",
                "الموقع التجريبي"
        );
        System.out.println("Sample Cheque Data created:");
        System.out.println("  Date: " + sampleCheque.getDate());
        System.out.println("  Beneficiary: " + sampleCheque.getBeneficiaryName());
        System.out.println("  Amount Numeric: " + sampleCheque.getAmountNumeric());
        System.out.println("  Amount Words: " + sampleCheque.getAmountWords());
        System.out.println("  Signer: " + sampleCheque.getSignerName());

        List<ChequeData> chequeDataList = new ArrayList<>();
        chequeDataList.add(sampleCheque);

        // 4. Generate PDF using PdfService
        PDDocument document = null;
        try {
            document = PdfService.createPdf(qnbBankTemplate.getTemplates().get(0), chequeDataList, "");
            System.out.println("PDF Document generated successfully.");

            // 5. Save the generated PDF for verification
            String outputFileName = "debug_qnb_cheque.pdf";
            document.save(outputFileName);
            System.out.println("Generated PDF saved to: " + outputFileName);

            // 6. Simulate printing (optional, as it requires a printer setup)
            // PdfPrinter.printPdf(List.of(document), qnbBankTemplate.getTemplates().get(0));
            // System.out.println("Simulated PDF printing.");

        } catch (IOException e) {
            System.err.println("Error generating or saving PDF: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) { // Catch PrinterException if uncommenting printPdf
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (document != null) {
                try {
                    // List<PDDocument> documentarray = new ArrayList<>();
                    // documentarray.add(document); 
                    // List<PDDocument> documentarrayy = documentarray ;
                    PdfPrinter.printPdf(document);
                    document.close();
                    System.out.println("PDF Document closed.");
                } catch (IOException e) {
                    System.err.println("Error closing PDF document: " + e.getMessage());
                }
            }
        }

        System.out.println("DebugMain finished.");
    }
}

