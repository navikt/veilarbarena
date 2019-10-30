package no.nav.fo.veilarbarena.utils;

import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.String.format;

public class VaultUtils {

    private static String DEFAULT_SECRETS_PATH = "/var/run/secrets/nais.io";
    private static String DEFAULT_CREDENTIALS_USERNAME_FILE = "username";
    private static String DEFAULT_CREDENTIALS_PASSWORD_FILE = "password";

    public static class Credentials {
        public final String username;
        public final String password;

        private Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static Credentials getCredentials(String credentialsPath,
                                             String usernameFileName,
                                             String passwordFileName) {
        Path path = Paths.get(credentialsPath);
        String username = getSecret(path.resolve(usernameFileName));
        String password = getSecret(path.resolve(passwordFileName));

        return new Credentials(username, password);
    }

    public static Credentials getCredentials(String path) {
        return getCredentials(path, DEFAULT_CREDENTIALS_USERNAME_FILE, DEFAULT_CREDENTIALS_PASSWORD_FILE);
    }

    public static String getSecret(String path) {
        return getSecret(Paths.get(path));
    }

    @SneakyThrows
    private static String getSecret(Path path) {
        List<String> strings;
        try {
            strings = Files.readAllLines(path);
        } catch (NoSuchFileException e) {
            throw fantIkkeSecret(path);
        }
        return strings.stream()
                .reduce((a, b) -> a + "\n" + b)
                .orElseThrow(() -> fantIkkeSecret(path));
    }

    private static IllegalStateException fantIkkeSecret(Path path) {
        return new IllegalStateException(format("Fant ikke secret %s", path.toString()));
    }

    public static String getDefaultSecretPath(String secret) {
        return Paths.get(DEFAULT_SECRETS_PATH, secret).toString();
    }
}

