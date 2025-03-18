package no.nav.veilarbarena.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ImportVaultEnvFiles {

    public static void main(String[] args) {
        File vaultDir = new File("/var/run/secrets/nais.io/vault");
        if (vaultDir.isDirectory()) {
            for (File file : vaultDir.listFiles((dir, name) -> name.endsWith(".env"))) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        int separatorIndex = line.indexOf('=');
                        if (separatorIndex != -1) {
                            String key = line.substring(0, separatorIndex);
                            String value = line.substring(separatorIndex + 1).replaceAll("^['\"]|['\"]$", "");
                            System.out.println("- exporting " + key);
                            setSystemProperty(key, value);
                        } else {
                            System.out.println("- (warn) exporting contents of " + file.getName() + " which is not formatted as KEY=VALUE");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
    }
}
