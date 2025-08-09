package org.chequePrinter.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;

import java.awt.print.*;
import java.io.File;
import java.io.IOException;

public class PdfPrinter {

    public static void printPdf(PDDocument document, float widthCm, float heightCm) throws IOException, PrinterException {
        if (document == null) {
            System.err.println("Document is null. Cannot print.");
            return;
        }

        // Convert dimensions from cm to inches, then to points
        float widthInches = widthCm / 2.54f;
        float heightInches = heightCm / 2.54f;

        float widthPoints = widthInches * 72f;
        float heightPoints = heightInches * 72f;

        System.out.println("=== PDFPRINTER DIMENSIONS DEBUG ===");
        System.out.println("Template dimensions from bank.json: " + widthCm + " x " + heightCm + " cm");
        System.out.println("Converted to points: " + widthPoints + " x " + heightPoints);

        // Configure paper
        Paper paper = new Paper();
        paper.setSize(heightPoints, widthPoints);
        paper.setImageableArea(0, 0, heightPoints , widthPoints); // No margins

        // Configure page format - Force PORTRAIT orientation as requested
        PageFormat pageFormat = new PageFormat();
        pageFormat.setPaper(paper);
        pageFormat.setOrientation(PageFormat.PORTRAIT);
        System.out.println("Orientation: PORTRAIT (forced as requested)");

        // Prepare the printer job
        PrinterJob job = PrinterJob.getPrinterJob();
        PDFPrintable printable = new PDFPrintable(document, Scaling.ACTUAL_SIZE);

        Book book = new Book();
        book.append(printable, pageFormat, document.getNumberOfPages());
        job.setPageable(book);

        // Open print dialog and print
        if (job.printDialog()) {
            job.print();
            System.out.println("PDF sent to printer successfully.");
        } else {
            System.out.println("Print job was cancelled.");
        }

        document.close();
    }

    public static void printPaymentPlanPdf(PDDocument document) throws IOException, PrinterException {
        if (document == null) {
            System.err.println("Document is null. Cannot print.");
            return;
        }

        // A4 Portrait dimensions: 21.0cm x 29.7cm
        float widthCm = 21.0f;
        float heightCm = 29.7f;
        
        // Convert dimensions from cm to inches, then to points
        float widthInches = widthCm / 2.54f;
        float heightInches = heightCm / 2.54f;

        float widthPoints = widthInches * 72f;
        float heightPoints = heightInches * 72f;

        System.out.println("=== PAYMENT PLAN PRINTER DEBUG ===");
        System.out.println("A4 Portrait dimensions: " + widthCm + " x " + heightCm + " cm");
        System.out.println("Converted to points: " + widthPoints + " x " + heightPoints);

        // Configure paper for A4 Portrait
        Paper paper = new Paper();
        paper.setSize(widthPoints, heightPoints); // Correct order for Portrait
        paper.setImageableArea(0, 0, widthPoints, heightPoints); // No margins

        // Configure page format - A4 Portrait
        PageFormat pageFormat = new PageFormat();
        pageFormat.setPaper(paper);
        pageFormat.setOrientation(PageFormat.PORTRAIT);
        System.out.println("Orientation: A4 PORTRAIT");

        // Prepare the printer job
        PrinterJob job = PrinterJob.getPrinterJob();
        PDFPrintable printable = new PDFPrintable(document, Scaling.ACTUAL_SIZE);

        Book book = new Book();
        book.append(printable, pageFormat, document.getNumberOfPages());
        job.setPageable(book);

        // Open print dialog and print
        if (job.printDialog()) {
            job.print();
            System.out.println("Payment Plan PDF sent to printer successfully.");
        } else {
            System.out.println("Print job was cancelled.");
        }

        document.close();
    }

    public static void main(String[] args) throws PrinterException, IOException {
        PDDocument doc = PDDocument.load(new File("debug_cheque_1754141199618.pdf"));
        PdfPrinter.printPdf(doc, 17.5f, 8.2f);
    }
}
