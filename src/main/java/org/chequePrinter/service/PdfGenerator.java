package org.chequePrinter.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.chequePrinter.model.PdfContent;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;

import java.io.IOException;
import java.util.List;
import java.io.File;
import java.io.InputStream;

public class PdfGenerator {
    /**
     * Shapes and reorders Arabic text for proper right-to-left rendering.
     *
     * @param text The Arabic text to shape and reorder.
     * @return The shaped and reordered text.
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

    public static PDDocument generatePdf(float pageWidth, float pageHeight, List<List<PdfContent>> allPagesContent, String fontPath) throws IOException {
        return generatePdf(pageWidth, pageHeight, allPagesContent, fontPath, null);
    }
    
    public static PDDocument generatePdf(float pageWidth, float pageHeight, List<List<PdfContent>> allPagesContent, String fontPath, String templateImagePath) throws IOException {
        // Template image path parameter is ignored - only text fields are printed
        return generatePdfTextOnly(pageWidth, pageHeight, allPagesContent, fontPath);
    }
    
    private static PDDocument generatePdfTextOnly(float pageWidth, float pageHeight, List<List<PdfContent>> allPagesContent, String fontPath) throws IOException {
        PDDocument document = new PDDocument();
        
        // Debug: Print the actual dimensions being used
        System.out.println("=== PDF GENERATION DEBUG ===");
        System.out.println("Page Width in Points: " + pageWidth);
        System.out.println("Page Height in Points: " + pageHeight);
        System.out.println("Page Width in CM: " + (pageWidth * 2.54f / 72f));
        System.out.println("Page Height in CM: " + (pageHeight * 2.54f / 72f));
        System.out.println("============================");

        try {
            // Use system font that supports Arabic instead of the corrupted Amiri font
            PDType0Font font = null;
            
            System.out.println("Attempting to load system fonts for Arabic support...");
            
            try {
                // Try Arial Unicode MS first (best Arabic support)
                font = PDType0Font.load(document, new File("C:/Windows/Fonts/arialuni.ttf"));
                System.out.println("Using Arial Unicode MS font (best Arabic support)");
            } catch (Exception e) {
                System.err.println("Arial Unicode MS not found: " + e.getMessage());
                try {
                    // Try regular Arial
                    font = PDType0Font.load(document, new File("C:/Windows/Fonts/arial.ttf"));
                    System.out.println("Using system Arial font");
                } catch (Exception e2) {
                    System.err.println("Arial not found: " + e2.getMessage());
                    try {
                        // Try Tahoma (good Arabic support)
                        font = PDType0Font.load(document, new File("C:/Windows/Fonts/tahoma.ttf"));
                        System.out.println("Using system Tahoma font");
                    } catch (Exception e3) {
                        System.err.println("Tahoma not found: " + e3.getMessage());
                        try {
                            // Last resort: try the custom font
                            InputStream fontStream = PdfGenerator.class.getClassLoader().getResourceAsStream(fontPath);
                            if (fontStream != null) {
                                font = PDType0Font.load(document, fontStream);
                                System.out.println("Using custom font as last resort: " + fontPath);
                            }
                        } catch (Exception e4) {
                            System.err.println("All fonts failed: " + e4.getMessage());
                            throw new IOException("Could not load any suitable font for Arabic text");
                        }
                    }
                }
            }

            if (allPagesContent == null || allPagesContent.isEmpty()) {
                // If no content, add at least one blank page with custom dimensions
                PDRectangle customSize = new PDRectangle(pageWidth, pageHeight);
                PDPage blankPage = new PDPage(customSize);
                document.addPage(blankPage);
            } else {
                for (List<PdfContent> pageContents : allPagesContent) {
                    // Create PDRectangle with custom dimensions and use it directly in PDPage constructor
                    PDPage page = new PDPage(new PDRectangle(pageWidth, pageHeight));
                    
                    document.addPage(page);
                    
                    // Debug: Verify page dimensions
                    PDRectangle pageSize = page.getMediaBox();
                    System.out.println("=== PAGE CREATION DEBUG ===");
                    System.out.println("Requested dimensions: " + pageWidth + " x " + pageHeight + " points");
                    System.out.println("Requested dimensions: " + (pageWidth * 2.54f / 72f) + " x " + (pageHeight * 2.54f / 72f) + " cm");
                    System.out.println("Actual page dimensions: " + pageSize.getWidth() + " x " + pageSize.getHeight() + " points");
                    System.out.println("Actual page dimensions: " + (pageSize.getWidth() * 2.54f / 72f) + " x " + (pageSize.getHeight() * 2.54f / 72f) + " cm");
                    System.out.println("MediaBox: " + page.getMediaBox());
                    System.out.println("===========================");

                    try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                        // Add only text content (no background image)
                        if (pageContents != null && !pageContents.isEmpty()) {
                            for (PdfContent content : pageContents) {
                                contentStream.setFont(font, content.fontSize);
                                contentStream.beginText();
                                contentStream.newLineAtOffset(content.x, content.y);

                                // Process Arabic text
                                String processedText = shapeAndReorderArabicText(content.text);
                                contentStream.showText(processedText);
                                contentStream.endText();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Catch any exception during PDF generation and rethrow as IOException
            throw new IOException("Error generating PDF: " + e.getMessage(), e);
        }
        
        // Rotate all pages to the left
        for (PDPage page : document.getPages()) {
            page.setRotation(90); // 90 degrees counter-clockwise
        }

        // Final debug: Check document dimensions before returning
        System.out.println("=== FINAL DOCUMENT CHECK ===");
        if (document.getNumberOfPages() > 0) {
            org.apache.pdfbox.pdmodel.PDPage firstPage = document.getPage(0);
            org.apache.pdfbox.pdmodel.common.PDRectangle finalSize = firstPage.getMediaBox();
        }
        System.out.println("============================");
        
        return document;
    }
}
