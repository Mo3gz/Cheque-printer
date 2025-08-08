package org.chequePrinter.controller;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.chequePrinter.model.ChequeData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ChequeFilterController {

    @FXML
    private TextField filterBeneficiaryField;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    private final DateTimeFormatter dbDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private ChequeDataController dataController;

    public void initialize() {
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        // Setup filtering listeners
        filterBeneficiaryField.textProperty().addListener((obs, oldVal, newVal) -> applyDateFilter());
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyDateFilter());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyDateFilter());
    }

    @FXML
    public void applyDateFilter() {
        if (dataController == null) return;

        // Get the current filter values
        String beneficiaryFilter = filterBeneficiaryField.getText();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // Create a filtered list based on the original data
        FilteredList<ChequeData> filteredData = new FilteredList<>(dataController.getAllCheques());

        // Apply filters
        filteredData.setPredicate(cheque -> {
            // Filter by beneficiary name if provided
            if (beneficiaryFilter != null && !beneficiaryFilter.isEmpty()) {
                if (!cheque.getSignerName().toLowerCase().contains(beneficiaryFilter.toLowerCase())) {
                    return false;
                }
            }

            // Filter by date range if provided
            if (startDate != null || endDate != null) {
                try {
                    LocalDate chequeDate = LocalDate.parse(cheque.getDate(), dbDateFormatter);
                    if (startDate != null && chequeDate.isBefore(startDate)) {
                        return false;
                    }
                    if (endDate != null && chequeDate.isAfter(endDate)) {
                        return false;
                    }
                } catch (Exception e) {
                    // If date parsing fails, try alternative format
                    try {
                        LocalDate chequeDate = LocalDate.parse(cheque.getDate());
                        if (startDate != null && chequeDate.isBefore(startDate)) {
                            return false;
                        }
                        if (endDate != null && chequeDate.isAfter(endDate)) {
                            return false;
                        }
                    } catch (Exception ex) {
                        // If both parsing attempts fail, exclude this record
                        return false;
                    }
                }
            }

            return true;
        });

        // Apply the filtered list to the table
        dataController.setFilteredItems(filteredData);
    }

    public FilteredList<ChequeData> applyFilterToList(ObservableList<ChequeData> sourceList) {
        // Get the current filter values
        String beneficiaryFilter = filterBeneficiaryField.getText();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // Create a filtered list based on the provided data
        FilteredList<ChequeData> filteredData = new FilteredList<>(sourceList);

        // Apply filters
        filteredData.setPredicate(cheque -> {
            // Filter by beneficiary name if provided
            if (beneficiaryFilter != null && !beneficiaryFilter.isEmpty()) {
                if (!cheque.getSignerName().toLowerCase().contains(beneficiaryFilter.toLowerCase())) {
                    return false;
                }
            }

            // Filter by date range if provided
            if (startDate != null || endDate != null) {
                try {
                    LocalDate chequeDate = LocalDate.parse(cheque.getDate(), dbDateFormatter);
                    if (startDate != null && chequeDate.isBefore(startDate)) {
                        return false;
                    }
                    if (endDate != null && chequeDate.isAfter(endDate)) {
                        return false;
                    }
                } catch (Exception e) {
                    // If date parsing fails, try alternative format
                    try {
                        LocalDate chequeDate = LocalDate.parse(cheque.getDate());
                        if (startDate != null && chequeDate.isBefore(startDate)) {
                            return false;
                        }
                        if (endDate != null && chequeDate.isAfter(endDate)) {
                            return false;
                        }
                    } catch (Exception ex) {
                        // If both parsing attempts fail, exclude this record
                        return false;
                    }
                }
            }

            return true;
        });

        return filteredData;
    }

    @FXML
    public void clearFilters() {
        filterBeneficiaryField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        
        if (dataController != null) {
            dataController.resetToAllItems();
        }
    }

    public boolean hasActiveFilters() {
        return (filterBeneficiaryField.getText() != null && !filterBeneficiaryField.getText().trim().isEmpty()) ||
               startDatePicker.getValue() != null || 
               endDatePicker.getValue() != null;
    }

    public String getBeneficiaryFilter() {
        return filterBeneficiaryField.getText();
    }

    public LocalDate getStartDate() {
        return startDatePicker.getValue();
    }

    public LocalDate getEndDate() {
        return endDatePicker.getValue();
    }

    public void setBeneficiaryFilter(String filter) {
        filterBeneficiaryField.setText(filter);
    }

    public void setStartDate(LocalDate date) {
        startDatePicker.setValue(date);
    }

    public void setEndDate(LocalDate date) {
        endDatePicker.setValue(date);
    }

    // Dependency injection
    public void setDataController(ChequeDataController dataController) {
        this.dataController = dataController;
    }

    // Getters for UI components
    public TextField getFilterBeneficiaryField() { return filterBeneficiaryField; }
    public DatePicker getStartDatePicker() { return startDatePicker; }
    public DatePicker getEndDatePicker() { return endDatePicker; }
}