
import no.nav.apiapp.ApiApp;
import no.nav.fo.veilarbarena.config.ApplicationConfig;

import static no.nav.brukerdialog.security.Constants.OIDC_REDIRECT_URL_PROPERTY_NAME;
import static no.nav.dialogarena.aktor.AktorConfig.AKTOER_ENDPOINT_URL;
import static no.nav.fo.veilarbarena.config.ApplicationConfig.AKTOER_V2_ENDPOINTURL;
import static no.nav.fo.veilarbarena.config.ApplicationConfig.REDIRECT_URL_PROPERTY;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

public class Main {
    public static void main(String... args) {
        System.setProperty(AKTOER_ENDPOINT_URL, getRequiredProperty(AKTOER_V2_ENDPOINTURL));
        System.setProperty(OIDC_REDIRECT_URL_PROPERTY_NAME, getRequiredProperty(REDIRECT_URL_PROPERTY));

        ApiApp.runApp(ApplicationConfig.class, args);
    }
}
