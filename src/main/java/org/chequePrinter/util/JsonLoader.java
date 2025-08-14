package org.chequePrinter.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.chequePrinter.model.BankTemplate;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.List;

public class JsonLoader {
    private static final org.slf4j.Logger logger = LoggerUtil.getLogger(JsonLoader.class);

    public static BankTemplate loadBankTemplate(String path) throws IOException {
        Gson gson = new Gson();
        try (InputStream stream = JsonLoader.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Resource not found: " + path);
            }
            return gson.fromJson(new InputStreamReader(stream), BankTemplate.class);
        }
    }
    
    public static List<BankTemplate> loadBankTemplates(String path) throws IOException {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<BankTemplate>>(){}.getType();

        // 1. Try to load from user's AppData directory first
        String appDataDir = System.getProperty("user.home") + File.separator + "ChequePrinterData";
        File userBankJsonFile = new File(appDataDir, "bank.json");

        if (userBankJsonFile.exists()) {
            try (FileReader reader = new FileReader(userBankJsonFile)) {
                logger.info("Loading bank.json from user data directory: {}", userBankJsonFile.getAbsolutePath());
                return gson.fromJson(reader, listType);
            } catch (IOException e) {
                logger.warn("Failed to load bank.json from user data directory, falling back to classpath: {}", e.getMessage());
                // Fall through to load from classpath
            }
        }

        // 2. Fallback to loading from classpath (default resources)
        try (InputStream stream = JsonLoader.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Resource not found in classpath: " + path);
            }
            logger.info("Loading bank.json from classpath: {}", path);
            return gson.fromJson(new InputStreamReader(stream), listType);
        }
    }
}
