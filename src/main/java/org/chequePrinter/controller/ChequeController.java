package org.chequePrinter.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.chequePrinter.model.BankTemplate;
import org.chequePrinter.model.ChequeData;
import org.chequePrinter.service.DatabaseService;
import org.chequePrinter.service.PdfService;
import org.chequePrinter.util.ArabicNumberToWords;
import org.chequePrinter.util.JsonLoader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChequeController {

    @FXML
    private Pane previewPane;
    @FXML
    private ImageView chequeImageView;
    @FXML
    private Text dateText;
    @FXML
    private Text beneficiaryText;
    @FXML
    private Text amountWordsText;
    @FXML
    private Text amountText;
    @FXML
    private Text signerText;

    @FXML
    private DatePicker firstCheckDatePicker;
    @FXML
    private TextField beneficiaryField;
    @FXML
    private TextField amountField;
    @FXML
    private TextField amountWordsField;
    @FXML
    private TextField signerField;

    @FXML
    private TextField numChecksField;
    @FXML
    private ComboBox<String> intervalComboBox;

    @FXML
    private TableView<ChequeData> chequeTableView;
    @FXML
    private TableColumn<ChequeData, Integer> idColumn;
    @FXML
    private TableColumn<ChequeData, String> dateColumn;
    @FXML
    private TableColumn<ChequeData, String> beneficiaryColumn;
    @FXML
    private TableColumn<ChequeData, String> amountNumericColumn;
    @FXML
    private TableColumn<ChequeData, String> amountWordsColumn;
    @FXML
    private TableColumn<ChequeData, String> signerColumn;

    @FXML
    private TextField filterBeneficiaryField;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    private BankTemplate bankTemplate;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dbDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ObservableList<ChequeData> chequeDataList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadBankTemplate();
        setupUI();
        setupBindings();
        setupTableView();
        loadChequeRecords();
    }

    private void loadBankTemplate() {
        try {
            List<BankTemplate> bankTemplates = JsonLoader.loadBankTemplates("/bank.json");
            if (bankTemplates != null && !bankTemplates.isEmpty()) {
                bankTemplate = bankTemplates.get(0); // Use the first bank template (NBE)
            } else {
                throw new IOException("No bank templates found in bank.json");
            }
            String imagePath = bankTemplate.getTemplates().get(0).getImagePath();
            if (imagePath == null || getClass().getResourceAsStream(imagePath) == null) {
                showAlert("Error", "Cheque image not found at: " + imagePath + ".\nPlease make sure the image exists.");
                previewPane.setStyle("-fx-background-color: lightgray;");
                return;
            }
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            if (image.isError()) {
                showAlert("Error", "Failed to load cheque image. It may be corrupt or in an unsupported format.");
                previewPane.setStyle("-fx-background-color: lightgray;");
            } else {
                chequeImageView.setImage(image);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load bank template configuration (bank.json).");
        }
    }

    private void setupUI() {
        intervalComboBox.getItems().addAll("1 Month", "3 Months", "6 Months", "12 Months");
        firstCheckDatePicker.setValue(LocalDate.now());

        chequeImageView.fitWidthProperty().bind(previewPane.widthProperty());
        chequeImageView.fitHeightProperty().bind(previewPane.heightProperty());

        previewPane.widthProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        previewPane.heightProperty().addListener((obs, oldVal, newVal) -> updatePreview());
    }

    private void setupBindings() {
        beneficiaryField.textProperty().addListener((obs, old, val) -> updatePreview());
        amountField.textProperty().addListener((obs, old, val) -> {
            updatePreview();
            if (val != null && !val.isEmpty()) {
                try {
                    double amount = Double.parseDouble(val);
                    amountWordsField.setText(ArabicNumberToWords.convert(amount));
                } catch (NumberFormatException e) {
                    amountWordsField.clear();
                }
            }
        });
        signerField.textProperty().addListener((obs, old, val) -> updatePreview());
        firstCheckDatePicker.valueProperty().addListener((obs, old, val) -> updatePreview());

        filterBeneficiaryField.textProperty().addListener((obs, old, val) -> applyDateFilter());
    }

    private void setupTableView() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        beneficiaryColumn.setCellValueFactory(new PropertyValueFactory<>("beneficiaryName"));
        amountNumericColumn.setCellValueFactory(new PropertyValueFactory<>("amountNumeric"));
        amountWordsColumn.setCellValueFactory(new PropertyValueFactory<>("amountWords"));
        signerColumn.setCellValueFactory(new PropertyValueFactory<>("signerName"));

        dateColumn.setCellFactory(column -> new TableCell<ChequeData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(LocalDate.parse(item, dbDateFormatter).format(dateFormatter));
                }
            }
        });

        chequeTableView.setItems(chequeDataList);

        chequeTableView.setRowFactory(tv -> {
            TableRow<ChequeData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    ChequeData rowData = row.getItem();
                    populateForm(rowData);
                }
            });
            return row;
        });
    }

    private void loadChequeRecords() {
        chequeDataList.setAll(DatabaseService.getAllCheques());
    }

    private void populateForm(ChequeData chequeData) {
        firstCheckDatePicker.setValue(LocalDate.parse(chequeData.getDate(), dbDateFormatter));
        beneficiaryField.setText(chequeData.getBeneficiaryName());
        amountField.setText(chequeData.getAmountNumeric());
        amountWordsField.setText(chequeData.getAmountWords());
        signerField.setText(chequeData.getSignerName());
    }

    private void updatePreview() {
        BankTemplate.Template template = bankTemplate.getTemplates().get(0);
        double scaleX = previewPane.getWidth() / template.getWidth();
        double scaleY = previewPane.getHeight() / template.getHeight();

        updateText(dateText, firstCheckDatePicker.getValue().format(dateFormatter), template.getFields().get("dateField"), scaleX, scaleY);
        updateText(beneficiaryText, beneficiaryField.getText(), template.getFields().get("beneficiaryField"), scaleX, scaleY);
        updateText(amountWordsText, amountWordsField.getText(), template.getFields().get("amountWordsField"), scaleX, scaleY);
        updateText(amountText, amountField.getText(), template.getFields().get("amountField"), scaleX, scaleY);
        updateText(signerText, signerField.getText(), template.getFields().get("signerField"), scaleX, scaleY);
    }

    private void updateText(Text textNode, String text, BankTemplate.Field field, double scaleX, double scaleY) {
        textNode.setText(text);
        textNode.setX(field.getX() * scaleX);
        textNode.setY(field.getY() * scaleY);
        textNode.setStyle(String.format("-fx-font-size: %fpx;", field.getFontSize() * Math.min(scaleX, scaleY)));
    }

    @FXML
    private void printAndSaveSingleCheck() {
        printAndSave(1);
    }

    @FXML
    private void printAndSaveMultipleChecks() {
        try {
            int numChecks = Integer.parseInt(numChecksField.getText());
            printAndSave(numChecks);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number of checks.");
        }
    }

    private void printAndSave(int numChecks) {
        if (!validateInput()) {
            return;
        }

        try {
            BankTemplate.Template selectedTemplate = bankTemplate.getTemplates().get(0);

            if (selectedTemplate == null) {
                showAlert("Error", "Please select a valid bank template before printing.");
                return;
            }

            // Retrieve template dimensions
            float widthInCm = selectedTemplate.getWidth();
            float heightInCm = selectedTemplate.getHeight();

            List<ChequeData> chequesToPrint = getChequeDataList(numChecks);
            PDDocument document = PdfService.createPdf(selectedTemplate, chequesToPrint, intervalComboBox.getValue());

            org.chequePrinter.service.PdfPrinter.printPdf(document, widthInCm, heightInCm);

            // Save records to the database with the correct date format
            for (ChequeData cheque : chequesToPrint) {
                ChequeData chequeToSave = new ChequeData(
                    LocalDate.parse(cheque.getDate(), dateFormatter).format(dbDateFormatter),
                    cheque.getBeneficiaryName(),
                    cheque.getAmountNumeric(),
                    cheque.getAmountWords(),
                    cheque.getSignerName()
                );
                DatabaseService.saveCheque(chequeToSave);
            }

            loadChequeRecords();
            showAlert("Success", "Cheques printed and records saved successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to print or save cheques.");
        }
    }


    @FXML
    private void applyDateFilter() {
        FilteredList<ChequeData> filteredData = new FilteredList<>(chequeDataList, p -> true);

        filteredData.setPredicate(cheque -> {
            LocalDate chequeDate = LocalDate.parse(cheque.getDate(), dbDateFormatter);
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            String beneficiaryName = filterBeneficiaryField.getText();

            boolean dateFilter = (startDate == null || !chequeDate.isBefore(startDate)) && (endDate == null || !chequeDate.isAfter(endDate));
            boolean nameFilter = beneficiaryName == null || beneficiaryName.isEmpty() || cheque.getSignerName().toLowerCase().contains(beneficiaryName.toLowerCase());

            return dateFilter && nameFilter;
        });

        chequeTableView.setItems(filteredData);
    }


    private List<ChequeData> getChequeDataList(int numChecks) {
        List<ChequeData> chequeDataList = new ArrayList<>();
        LocalDate currentDate = firstCheckDatePicker.getValue();
        for (int i = 0; i < numChecks; i++) {
            chequeDataList.add(new ChequeData(
                    currentDate.format(dateFormatter),
                    beneficiaryField.getText(),
                    amountField.getText(),
                    amountWordsField.getText(),
                    signerField.getText()
            ));
            currentDate = getNextDate(currentDate, intervalComboBox.getValue());
        }
        return chequeDataList;
    }

    private LocalDate getNextDate(LocalDate date, String interval) {
        if (interval == null) {
            return date;
        }
        switch (interval) {
            case "1 Month":
                return date.plusMonths(1);
            case "3 Months":
                return date.plusMonths(3);
            case "6 Months":
                return date.plusMonths(6);
            case "12 Months":
                return date.plusYears(1);
            default:
                return date;
        }
    }

    private boolean validateInput() {
        if (beneficiaryField.getText().isEmpty() || amountField.getText().isEmpty() || signerField.getText().isEmpty()) {
            showAlert("Invalid Input", "Please fill all fields.");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
