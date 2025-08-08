package org.chequePrinter.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chequePrinter.model.ChequeData;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;

public class ChequeExportController {

    private ChequeDataController dataController;
    private ChequeFilterController filterController;
    private Stage parentStage;

    public void initialize() {
        // No FXML components to initialize in this controller
    }

    @FXML
    public void exportSelectedToExcel() {
        if (dataController == null) {
            showAlert("Error", "Data controller not available.");
            return;
        }

        ObservableList<ChequeData> selectedCheques = dataController.getSelectedCheques();
        if (selectedCheques.isEmpty()) {
            showAlert("No Selection", "Please select one or more cheques to export.");
            return;
        }
        exportToExcel(selectedCheques, "Selected_Cheques_" + LocalDate.now().toString());
    }

    @FXML
    public void exportFilteredToExcel() {
        if (dataController == null) {
            showAlert("Error", "Data controller not available.");
            return;
        }

        // Get the filtered items from the table
        ObservableList<ChequeData> filteredCheques = dataController.getChequeTableView().getItems();
        if (filteredCheques.isEmpty()) {
            showAlert("No Data", "No filtered cheques to export.");
            return;
        }
        exportToExcel(filteredCheques, "Filtered_Cheques_" + LocalDate.now().toString());
    }

    @FXML
    public void exportAllToExcel() {
        if (dataController == null) {
            showAlert("Error", "Data controller not available.");
            return;
        }

        ObservableList<ChequeData> allCheques = dataController.getAllCheques();
        if (allCheques.isEmpty()) {
            showAlert("No Data", "No cheques to export.");
            return;
        }
        exportToExcel(allCheques, "All_Cheques_" + LocalDate.now().toString());
    }

    public void exportToExcel(ObservableList<ChequeData> cheques, String defaultFileName) {
        try {
            // Create a new workbook and sheet
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Cheques");
            
            // Create header row
            String[] headers = {"ID", "Date", "Beneficiary", "Amount", "Amount in Words", "Signer"};
            Row headerRow = sheet.createRow(0);
            
            // Style for header row
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Create header cells
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                // Auto-size the column
                sheet.autoSizeColumn(i);
            }
            
            // Create data rows
            int rowNum = 1;
            for (ChequeData cheque : cheques) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(cheque.getId());
                row.createCell(1).setCellValue(cheque.getDate());
                row.createCell(2).setCellValue(cheque.getBeneficiaryName());
                row.createCell(3).setCellValue(cheque.getAmountNumeric());
                row.createCell(4).setCellValue(cheque.getAmountWords());
                row.createCell(5).setCellValue(cheque.getSignerName());
            }
            
            // Auto-size all columns after data is added
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Excel File");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );
            fileChooser.setInitialFileName(defaultFileName + ".xlsx");
            
            // Show save dialog
            Stage stage = getParentStage();
            File file = fileChooser.showSaveDialog(stage);
            
            if (file != null) {
                // Write the workbook to file
                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    workbook.write(outputStream);
                    showAlert("Success", "Cheques exported successfully to: " + file.getAbsolutePath());
                }
            }
            
            // Close the workbook
            workbook.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Export Error", "An error occurred while exporting to Excel: " + e.getMessage());
        }
    }

    public void exportCustomToExcel(ObservableList<ChequeData> cheques, String fileName, String sheetName) {
        try {
            // Create a new workbook and sheet
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet(sheetName != null ? sheetName : "Cheques");
            
            // Create header row
            String[] headers = {"ID", "Date", "Beneficiary", "Amount", "Amount in Words", "Signer"};
            Row headerRow = sheet.createRow(0);
            
            // Style for header row
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Create header cells
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (ChequeData cheque : cheques) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(cheque.getId());
                row.createCell(1).setCellValue(cheque.getDate());
                row.createCell(2).setCellValue(cheque.getBeneficiaryName());
                row.createCell(3).setCellValue(cheque.getAmountNumeric());
                row.createCell(4).setCellValue(cheque.getAmountWords());
                row.createCell(5).setCellValue(cheque.getSignerName());
            }
            
            // Auto-size all columns after data is added
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write the workbook to file
            try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
                workbook.write(outputStream);
                showAlert("Success", "Cheques exported successfully to: " + fileName);
            }
            
            // Close the workbook
            workbook.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Export Error", "An error occurred while exporting to Excel: " + e.getMessage());
        }
    }

    private Stage getParentStage() {
        if (parentStage != null) {
            return parentStage;
        }
        
        // Try to get stage from data controller's table view
        if (dataController != null && dataController.getChequeTableView() != null && 
            dataController.getChequeTableView().getScene() != null) {
            return (Stage) dataController.getChequeTableView().getScene().getWindow();
        }
        
        return null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Dependency injection methods
    public void setDataController(ChequeDataController dataController) {
        this.dataController = dataController;
    }

    public void setFilterController(ChequeFilterController filterController) {
        this.filterController = filterController;
    }

    public void setParentStage(Stage parentStage) {
        this.parentStage = parentStage;
    }
}