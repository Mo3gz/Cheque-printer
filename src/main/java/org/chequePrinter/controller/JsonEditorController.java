package org.chequePrinter.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import org.chequePrinter.util.ExceptionHandler;
import org.chequePrinter.util.LoggerUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

public class JsonEditorController {

    @FXML
    private TextArea jsonTextArea;

    private File userBankJsonFile;
    private static final org.slf4j.Logger logger = LoggerUtil.getLogger(JsonEditorController.class);

    @FXML
    public void initialize() {
        initializeUserBankJsonFile();
        loadJsonFile();
    }

    private void initializeUserBankJsonFile() {
        String appDataDir = System.getProperty("user.home") + File.separator + "ChequePrinterData";
        File directory = new File(appDataDir);
        if (!directory.exists()) {
            directory.mkdirs(); // Create the directory if it doesn't exist
        }
        userBankJsonFile = new File(directory, "bank.json");
    }

    private void loadJsonFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonContent = null;

        // Try to load from user's AppData directory first
        if (userBankJsonFile.exists()) {
            try (FileReader reader = new FileReader(userBankJsonFile)) {
                jsonContent = new String(Files.readAllBytes(userBankJsonFile.toPath()));
                logger.info("Loaded bank.json from user data directory: {}", userBankJsonFile.getAbsolutePath());
            } catch (IOException e) {
                ExceptionHandler.handleException(logger, "loadJsonFile", e, "Error loading bank.json from user data directory.");
            }
        }

        // If not loaded from user data or error, load from classpath (default)
        if (jsonContent == null) {
            try (InputStream input = getClass().getResourceAsStream("/bank.json")) {
                if (input == null) {
                    showAlert(Alert.AlertType.ERROR, "File Not Found", "Default bank.json not found in application resources.");
                    logger.error("Default bank.json not found in classpath.");
                    return;
                }
                jsonContent = new String(input.readAllBytes());
                logger.info("Loaded bank.json from classpath (default).");
            } catch (IOException e) {
                ExceptionHandler.handleException(logger, "loadJsonFile", e, "Error loading bank.json from classpath.");
                return;
            }
        }

        if (jsonContent != null) {
            try {
                Object json = gson.fromJson(jsonContent, Object.class);
                jsonTextArea.setText(gson.toJson(json));
            } catch (JsonParseException e) {
                showAlert(Alert.AlertType.ERROR, "JSON Parse Error", "The bank.json file contains invalid JSON. Please correct it.");
                ExceptionHandler.handleException(logger, "loadJsonFile", e, "Invalid JSON format in bank.json.");
            }
        }
    }

    @FXML
    private void handleLoad() {
        loadJsonFile();
    }

    @FXML
    private void handleSave() {
        String newJsonContent = jsonTextArea.getText();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            // Validate JSON
            Object json = gson.fromJson(newJsonContent, Object.class);
            // Pretty print again to ensure consistent formatting before saving
            String formattedJson = gson.toJson(json);

            // Ensure the directory exists
            if (!userBankJsonFile.getParentFile().exists()) {
                userBankJsonFile.getParentFile().mkdirs();
            }

            Files.write(userBankJsonFile.toPath(), formattedJson.getBytes());
            showAlert(Alert.AlertType.INFORMATION, "Save Successful", "bank.json saved successfully to: " + userBankJsonFile.getAbsolutePath());
            logger.info("Saved JSON to: {}", userBankJsonFile.getAbsolutePath());
        } catch (JsonParseException e) {
            showAlert(Alert.AlertType.ERROR, "JSON Validation Error", "Invalid JSON format. Please correct it before saving.");
            ExceptionHandler.handleException(logger, "handleSave", e, "Attempted to save invalid JSON.");
        } catch (IOException e) {
            ExceptionHandler.handleException(logger, "handleSave", e, "Error saving JSON file.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}