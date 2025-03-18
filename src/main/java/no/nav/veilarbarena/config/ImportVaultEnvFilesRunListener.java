package no.nav.veilarbarena.config;

import no.nav.veilarbarena.utils.ImportVaultEnvFiles;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;

public class ImportVaultEnvFilesRunListener implements SpringApplicationRunListener {

    public ImportVaultEnvFilesRunListener(SpringApplication application, String[] args) {
        // Constructor required by SpringApplicationRunListener
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        ImportVaultEnvFiles.main(new String[]{});
    }

}