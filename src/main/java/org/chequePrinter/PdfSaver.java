package org.chequePrinter;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;

public class PdfSaver {

    /**
     * Saves a PDF document to the specified path.
     *
     * @param path     The file path where the PDF should be saved.
     * @param document The PDF document to save.
     */
    public void save(String path, Document document) {
        try {
            PdfWriter.getInstance(document, new FileOutputStream(path));
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }
}
