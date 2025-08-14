package org.chequePrinter.license;

import java.util.prefs.Preferences;

public class LicenseWriter {

    private static final String REGISTRY_PATH = "org/chequePrinter";
    private static final String LICENSE_KEY = "licenseText";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar license-writer.jar <LICENSE_TEXT>");
            System.out.println("Example: java -jar license-writer.jar MY_SECRET_LICENSE_KEY_123");
            return;
        }

        String licenseText = args[0];
        Preferences prefs = Preferences.userRoot().node(REGISTRY_PATH);

        try {
            prefs.put(LICENSE_KEY, licenseText);
            System.out.println("License text successfully written to registry.");
                        System.out.println("Key: HKEY_CURRENT_USER\\SOFTWARE\\JavaSoft\\Prefs\\" + REGISTRY_PATH.replace('/', '\\') + "\\" + LICENSE_KEY);
            System.out.println("Value: " + licenseText);
        } catch (Exception e) {
            System.err.println("Error writing license text to registry: " + e.getMessage());
            e.printStackTrace();
        }
    }

}