package org.chequePrinter.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.chequePrinter.model.BankTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

public class JsonLoader {
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
        try (InputStream stream = JsonLoader.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Resource not found: " + path);
            }
            Type listType = new TypeToken<List<BankTemplate>>(){}.getType();
            return gson.fromJson(new InputStreamReader(stream), listType);
        }
    }
}
