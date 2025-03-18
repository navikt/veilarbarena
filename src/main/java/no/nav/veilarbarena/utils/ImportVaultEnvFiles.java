package no.nav.veilarbarena.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class ImportVaultEnvFiles {
    static final Logger logger = LoggerFactory.getLogger(ImportVaultEnvFiles.class);

    public static void main(String[] args) {
        File vaultDir = new File("/var/run/secrets/nais.io/vault");
        if (vaultDir.isDirectory()) {
            for (File file : Objects.requireNonNull(vaultDir.listFiles((dir, name) -> name.endsWith(".env")))) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        int separatorIndex = line.indexOf('=');
                        if (separatorIndex != -1) {
                            String key = line.substring(0, separatorIndex);
                            String value = line.substring(separatorIndex + 1).replaceAll("(^['\"]|['\"]$)", "");
                            logger.info("- exporting {}", key);
                            setSystemProperty(key, value);
                        } else {
                            logger.warn("- (warn) exporting contents of {} which is not formatted as KEY=VALUE", file.getName());
                        }
                    }
                } catch (IOException e) {
                    logger.error("Error while reading vault values", e);
                }
            }
        }
    }

    private static void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
    }
}
