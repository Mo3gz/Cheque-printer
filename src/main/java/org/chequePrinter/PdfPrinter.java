package org.chequePrinter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.chequePrinter.model.BankTemplate;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.OrientationRequested;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.IOException;

public class PdfPrinter implements Printable {
    
    private PDDocument document;
    private BankTemplate.Template template;
    private PDFRenderer pdfRenderer;
    
    public PdfPrinter(PDDocument document, BankTemplate.Template template) {
        this.document = document;
        this.template = template;
        this.pdfRenderer = new PDFRenderer(document);
    }

    public void printPage() {
        if (document == null) {
            System.out.println("No documents to print.");
            return;
        }
        if (template == null) {
            System.out.println("BankTemplate.Template is null. Cannot determine page dimensions for printing.");
            return;
        }

        PrinterJob job = PrinterJob.getPrinterJob();
        
        // Convert template dimensions from cm to points (1 cm = 28.35 points)
        float widthInCm = template.getWidth();
        float heightInCm = template.getHeight();
        
        // Convert cm to points (1 inch = 72 points, 1 cm = 0.393701 inches)
        double widthInPoints = widthInCm * 28.35;
        double heightInPoints = heightInCm * 28.35;
        
        System.out.println("Width in cm: " + widthInCm);
        System.out.println("Height in cm: " + heightInCm);
        System.out.println("Width in points: " + widthInPoints);
        System.out.println("Height in points: " + heightInPoints);
        
        // Create custom paper size
        Paper customPaper = new Paper();
        customPaper.setSize(widthInPoints, heightInPoints);
        customPaper.setImageableArea(0, 0, widthInPoints, heightInPoints);
        
        // Create page format with custom paper
        PageFormat pf = new PageFormat();
        pf.setPaper(customPaper);
        
        // Set orientation based on template dimensions
        if (widthInCm > heightInCm) {
            pf.setOrientation(PageFormat.LANDSCAPE);
        } else {
            pf.setOrientation(PageFormat.PORTRAIT);
        }
        
        // Validate the page format with the printer
        PageFormat validatedPf = job.validatePage(pf);
        
        // Try to force custom size with print attributes as well
        PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
        float widthInInches = widthInCm * 0.393701f;
        float heightInInches = heightInCm * 0.393701f;
        
        // Add printable area to hint at custom size
        attrs.add(new MediaPrintableArea(0, 0, widthInInches, heightInInches, MediaPrintableArea.INCH));
        
        if (widthInCm > heightInCm) {
            attrs.add(OrientationRequested.LANDSCAPE);
        } else {
            attrs.add(OrientationRequested.PORTRAIT);
        }
        
        Book book = new Book();
        book.append(this, validatedPf);
        job.setPageable(book);
        
        boolean ok = job.printDialog();
        if (ok) {
            try {
                // Print with attributes to force custom size
                job.print(attrs);
                System.out.println("PDF sent to printer successfully with custom dimensions: " + widthInCm + "cm x " + heightInCm + "cm");
            } catch (PrinterException ex) {
                System.out.println("Print error: " + ex.getMessage());
                // Fallback to print without attributes
                try {
                    job.print();
                    System.out.println("PDF sent to printer successfully (fallback mode).");
                } catch (PrinterException ex2) {
                    System.out.println("Print error (fallback): " + ex2.getMessage());
                }
            } finally {
                if (document != null) {
                    try {
                        document.close();
                    } catch (IOException e) {
                        System.out.println("Error closing document: " + e.getMessage());
                    }
                }
            }
        }
    }
    
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex >= document.getNumberOfPages()) {
            return NO_SUCH_PAGE;
        }
        
        try {
            // Render PDF page as BufferedImage
            BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 150); // 150 DPI for good quality
            
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            // Get the printable area
            double pageWidth = pageFormat.getImageableWidth();
            double pageHeight = pageFormat.getImageableHeight();
            
            // Scale image to fit the printable area while maintaining aspect ratio
            double scaleX = pageWidth / image.getWidth();
            double scaleY = pageHeight / image.getHeight();
            double scale = Math.min(scaleX, scaleY);
            
            int scaledWidth = (int) (image.getWidth() * scale);
            int scaledHeight = (int) (image.getHeight() * scale);
            
            // Center the image on the page
            int x = (int) (pageFormat.getImageableX() + (pageWidth - scaledWidth) / 2);
            int y = (int) (pageFormat.getImageableY() + (pageHeight - scaledHeight) / 2);
            
            g2d.drawImage(image, x, y, scaledWidth, scaledHeight, null);
            
            return PAGE_EXISTS;
        } catch (IOException e) {
            throw new PrinterException("Error rendering PDF page: " + e.getMessage());
        }
    }
    
    // Static method to maintain compatibility with existing code
    public static void printPdf(PDDocument document, BankTemplate.Template template) throws IOException, PrinterException {
        PdfPrinter printer = new PdfPrinter(document, template);
        printer.printPage();
    }
}