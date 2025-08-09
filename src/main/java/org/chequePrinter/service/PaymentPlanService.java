package org.chequePrinter.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.chequePrinter.model.ChequeData;
import org.chequePrinter.util.ArabicNumberToWords;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PaymentPlanService {
    
    private static final float MARGIN = 50;
    private static final float TITLE_FONT_SIZE = 18;
    private static final float HEADER_FONT_SIZE = 14;
    private static final float CONTENT_FONT_SIZE = 12;
    private static final float LINE_HEIGHT = 25; // Increased for amount in words
    private static final int MAX_CHEQUES_PER_PAGE = 20; // Reduced due to larger line height
    
    /**
     * Shapes and reorders Arabic text for proper right-to-left rendering.
     */
    private static String shapeAndReorderArabicText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        try {
            // Check if text contains Arabic characters
            boolean hasArabic = text.chars().anyMatch(c -> c >= 0x0600 && c <= 0x06FF);
            
            if (!hasArabic) {
                // If no Arabic characters, return as is
                return text;
            }
            
            // 1. Shape the Arabic letters
            ArabicShaping shaper = new ArabicShaping(ArabicShaping.LETTERS_SHAPE | ArabicShaping.LENGTH_GROW_SHRINK);
            String shapedText = shaper.shape(text);

            // 2. Reorder the text for right-to-left display
            Bidi bidi = new Bidi(shapedText, Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT);
            String reorderedText = bidi.writeReordered(Bidi.DO_MIRRORING);
            
            return reorderedText;

        } catch (ArabicShapingException e) {
            System.err.println("Arabic shaping failed for text: " + text);
            e.printStackTrace();
            return text; // Return original text if shaping fails
        } catch (Exception e) {
            System.err.println("Error processing Arabic text: " + text);
            e.printStackTrace();
            return text;
        }
    }
    
    public static PDDocument generatePaymentPlanPDF(List<ChequeData> cheques, String signerName) throws IOException {
        PDDocument document = new PDDocument();
        
        if (cheques.isEmpty()) {
            // Create a single page with "No cheques found" message
            createEmptyPage(document, signerName);
            return document;
        }
        
        // Calculate total amount for ALL cheques
        double totalAmount = cheques.stream()
            .mapToDouble(c -> {
                try {
                    return Double.parseDouble(c.getAmountNumeric());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            })
            .sum();
        
        // Calculate total pages needed
        int totalPages = (int) Math.ceil((double) cheques.size() / MAX_CHEQUES_PER_PAGE);
        
        for (int pageNum = 0; pageNum < totalPages; pageNum++) {
            int startIndex = pageNum * MAX_CHEQUES_PER_PAGE;
            int endIndex = Math.min(startIndex + MAX_CHEQUES_PER_PAGE, cheques.size());
            List<ChequeData> pageData = cheques.subList(startIndex, endIndex);
            
            createPaymentPlanPage(document, pageData, signerName, pageNum + 1, totalPages, totalAmount, cheques.size());
        }
        
        return document;
    }
    
    private static void createEmptyPage(PDDocument document, String signerName) throws IOException {
        // Create A4 Portrait page explicitly
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            PDType0Font font = loadFont(document);
            float yPosition = page.getMediaBox().getHeight() - MARGIN;
            
            // Title
            contentStream.beginText();
            contentStream.setFont(font, TITLE_FONT_SIZE);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(shapeAndReorderArabicText("Payment Plan"));
            contentStream.endText();
            
            yPosition -= 40;
            
            // Signer name
            contentStream.beginText();
            contentStream.setFont(font, HEADER_FONT_SIZE);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(shapeAndReorderArabicText("Signer: " + (signerName != null ? signerName : "N/A")));
            contentStream.endText();
            
            yPosition -= 40;
            
            // No cheques message
            contentStream.beginText();
            contentStream.setFont(font, CONTENT_FONT_SIZE);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(shapeAndReorderArabicText("No cheques found in the editing table."));
            contentStream.endText();
        }
    }
    
    private static void createPaymentPlanPage(PDDocument document, List<ChequeData> cheques,
                                            String signerName, int currentPage, int totalPages, double totalAmount, int totalCheques) throws IOException {
        // Create A4 Portrait page explicitly
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            PDType0Font font = loadFont(document);
            float yPosition = page.getMediaBox().getHeight() - MARGIN;
            
            // Title
            contentStream.beginText();
            contentStream.setFont(font, TITLE_FONT_SIZE);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(shapeAndReorderArabicText("Payment Plan"));
            contentStream.endText();
            
            yPosition -= 30;
            
            // Generation date and time
            contentStream.beginText();
            contentStream.setFont(font, CONTENT_FONT_SIZE);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            contentStream.showText(shapeAndReorderArabicText("Generated on: " + currentDateTime));
            contentStream.endText();
            
            yPosition -= 25;
            
            // Signer name
            contentStream.beginText();
            contentStream.setFont(font, HEADER_FONT_SIZE);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(shapeAndReorderArabicText("Signer: " + (signerName != null ? signerName : "N/A")));
            contentStream.endText();
            
            yPosition -= 30;
            
            // Page info
            if (totalPages > 1) {
                contentStream.beginText();
                contentStream.setFont(font, CONTENT_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText(shapeAndReorderArabicText("Page " + currentPage + " of " + totalPages));
                contentStream.endText();
                yPosition -= 25;
            }
            
            // Table headers (added Amount in Words column)
            contentStream.beginText();
            contentStream.setFont(font, HEADER_FONT_SIZE);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(shapeAndReorderArabicText("No."));
            contentStream.newLineAtOffset(50, 0);
            contentStream.showText(shapeAndReorderArabicText("Date"));
            contentStream.newLineAtOffset(100, 0);
            contentStream.showText(shapeAndReorderArabicText("Amount"));
            contentStream.newLineAtOffset(120, 0);
            contentStream.showText(shapeAndReorderArabicText("Amount in Words"));
            contentStream.endText();
            
            yPosition -= 25;
            
            // Draw line under headers
            contentStream.moveTo(MARGIN, yPosition);
            contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, yPosition);
            contentStream.stroke();
            
            yPosition -= 10;
            
            // Cheque data
            int startIndex = (currentPage - 1) * MAX_CHEQUES_PER_PAGE;
            for (int i = 0; i < cheques.size(); i++) {
                ChequeData cheque = cheques.get(i);
                int chequeNumber = startIndex + i + 1;
                
                contentStream.beginText();
                contentStream.setFont(font, CONTENT_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText(shapeAndReorderArabicText(String.valueOf(chequeNumber)));
                contentStream.newLineAtOffset(50, 0);
                contentStream.showText(shapeAndReorderArabicText(cheque.getDate() != null ? cheque.getDate() : "N/A"));
                contentStream.newLineAtOffset(100, 0);
                contentStream.showText(shapeAndReorderArabicText(cheque.getAmountNumeric() != null ? cheque.getAmountNumeric() : "N/A"));
                contentStream.newLineAtOffset(120, 0);
                
                // Convert amount to Arabic words and add "جنيها مصريا لا غير"
                String amountInWords = "";
                if (cheque.getAmountNumeric() != null && !cheque.getAmountNumeric().isEmpty()) {
                    try {
                        double amount = Double.parseDouble(cheque.getAmountNumeric());
                        amountInWords = ArabicNumberToWords.convert(amount) + " جنيها مصريا لا غير";
                    } catch (NumberFormatException e) {
                        amountInWords = "مبلغ غير صحيح";
                    }
                } else {
                    amountInWords = "N/A";
                }
                
                // Handle long Arabic text by wrapping to multiple lines
                if (amountInWords.length() > 35) {
                    // Split at word boundaries for Arabic text
                    String[] words = amountInWords.split(" ");
                    StringBuilder firstLine = new StringBuilder();
                    StringBuilder secondLine = new StringBuilder();
                    
                    boolean useSecondLine = false;
                    for (String word : words) {
                        if (!useSecondLine && (firstLine.length() + word.length() + 1) <= 35) {
                            if (firstLine.length() > 0) firstLine.append(" ");
                            firstLine.append(word);
                        } else {
                            useSecondLine = true;
                            if (secondLine.length() > 0) secondLine.append(" ");
                            secondLine.append(word);
                        }
                    }
                    
                    // Display first line
                    contentStream.showText(shapeAndReorderArabicText(firstLine.toString()));
                    contentStream.endText();
                    
                    // Display second line if needed
                    if (secondLine.length() > 0) {
                        yPosition -= 12; // Move down for second line
                        contentStream.beginText();
                        contentStream.setFont(font, CONTENT_FONT_SIZE);
                        contentStream.newLineAtOffset(MARGIN + 270, yPosition); // Same position as Amount in Words column
                        contentStream.showText(shapeAndReorderArabicText(secondLine.toString()));
                        contentStream.endText();
                    }
                } else {
                    // Short text, display normally
                    contentStream.showText(shapeAndReorderArabicText(amountInWords));
                    contentStream.endText();
                }
                
                yPosition -= LINE_HEIGHT; // Move to next line for next record
                
                // Check if we need to break to next page (shouldn't happen with proper pagination)
                if (yPosition < MARGIN + 50) {
                    break;
                }
            }
            
            // Summary at bottom
            yPosition -= 20;
            if (yPosition > MARGIN + 30) {
                contentStream.beginText();
                contentStream.setFont(font, HEADER_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                
                if (currentPage == totalPages) {
                    // Show total for ALL cheques on last page
                    contentStream.showText(shapeAndReorderArabicText("Total Cheques: " + totalCheques +
                                         " | Total Amount: " + String.format("%.2f", totalAmount)));
                } else {
                    contentStream.showText(shapeAndReorderArabicText("Cheques on this page: " + cheques.size()));
                }
                contentStream.endText();
            }
        }
    }
    
    private static PDType0Font loadFont(PDDocument document) throws IOException {
        PDType0Font font = null;
        
        try {
            // Try Arial Unicode MS first (best Arabic support)
            font = PDType0Font.load(document, new File("C:/Windows/Fonts/arialuni.ttf"));
        } catch (Exception e) {
            try {
                // Try regular Arial
                font = PDType0Font.load(document, new File("C:/Windows/Fonts/arial.ttf"));
            } catch (Exception e2) {
                try {
                    // Try Tahoma (good Arabic support)
                    font = PDType0Font.load(document, new File("C:/Windows/Fonts/tahoma.ttf"));
                } catch (Exception e3) {
                    try {
                        // Last resort: try the custom font
                        InputStream fontStream = PaymentPlanService.class.getClassLoader().getResourceAsStream("Amiri-Regular.ttf");
                        if (fontStream != null) {
                            font = PDType0Font.load(document, fontStream);
                        }
                    } catch (Exception e4) {
                        throw new IOException("Could not load any suitable font for text");
                    }
                }
            }
        }
        
        if (font == null) {
            throw new IOException("Could not load any suitable font for text");
        }
        
        return font;
    }
}