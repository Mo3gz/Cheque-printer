package org.chequePrinter.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.printing.PDFPageable;
import org.chequePrinter.model.BankTemplate;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.MediaPrintableArea;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.util.List;

public class PdfPrinter {

    public static void printPdf(List<PDDocument> documents, BankTemplate.Template template) throws IOException, PrinterException {
        if (documents == null || documents.isEmpty()) {
            System.out.println("No documents to print.");
            return;
        }
        if (template == null) {
            System.out.println("BankTemplate.Template is null. Cannot determine page dimensions for printing.");
            return;
        }

        PrinterJob job = PrinterJob.getPrinterJob();

        // Get dimensions from the template
        float widthInPoints = template.getWidth();
        float heightInPoints = template.getHeight();

        // Convert points to inches (1 inch = 72 points)
        float widthInInches = widthInPoints / 72f;
        float heightInInches = heightInPoints / 72f;

        PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();

        // Set orientation based on template dimensions
        if (widthInPoints > heightInPoints) {
            attr.add(OrientationRequested.LANDSCAPE);
        } else {
            attr.add(OrientationRequested.PORTRAIT);
        }

        // Set margins to zero by setting the printable area to the full page size
        attr.add(new MediaPrintableArea(0, 0, widthInInches, heightInInches, MediaPrintableArea.INCH));

        for (PDDocument document : documents) {
            try {
                job.setPageable(new PDFPageable(document));
                job.print(attr);
                System.out.println("PDF sent to printer successfully.");
            } finally {
                if (document != null) {
                    document.close();
                }
            }
        }
    }

     public static void printPdf(PDDocument document) throws IOException, PrinterException {
        if (document == null) {
            System.out.println("Document to print is null.");
            return;
        }

        PrinterJob job = PrinterJob.getPrinterJob();

        // Get dimensions from the PDDocument's first page
        PDPage page = document.getPage(0);
        PDRectangle mediaBox = page.getMediaBox();
        float widthInPoints = mediaBox.getWidth();
        float heightInPoints = mediaBox.getHeight();

        // Convert points to inches (1 inch = 72 points)
        float widthInInches = widthInPoints / 72f;
        float heightInInches = heightInPoints / 72f;

        PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();

        // Set orientation based on document dimensions
        if (widthInPoints > heightInPoints) {
            attr.add(OrientationRequested.LANDSCAPE);
        } else {
            attr.add(OrientationRequested.PORTRAIT);
        }

        // Set margins to zero by setting the printable area to the full page size
        attr.add(new MediaPrintableArea(0, 0, widthInInches, heightInInches, MediaPrintableArea.INCH));

        try {
            job.setPageable(new PDFPageable(document));
            job.print(attr);
            System.out.println("PDF sent to printer successfully.");
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }


}

