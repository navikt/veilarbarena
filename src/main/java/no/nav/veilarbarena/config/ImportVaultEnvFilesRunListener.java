package no.nav.veilarbarena.config;

import no.nav.veilarbarena.utils.ImportVaultEnvFiles;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;

public class ImportVaultEnvFilesRunListener implements SpringApplicationRunListener {

    public ImportVaultEnvFilesRunListener(SpringApplication application, String[] args) {
        // Constructor required by SpringApplicationRunListener
    }

    @Override
    public void starting(@NotNull ConfigurableBootstrapContext bootstrapContext) {
        ImportVaultEnvFiles.main(new String[]{});
    }

}