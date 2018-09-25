import no.nav.apiapp.ApiApp;
import no.nav.fo.veilarbarena.utils.MigrationUtils;
import no.nav.fo.veilarbarena.config.ApplicationConfig;
import no.nav.fo.veilarbarena.config.DbConfig;
import no.nav.sbl.dialogarena.common.cxf.StsSecurityConstants;

import static no.nav.brukerdialog.security.Constants.OIDC_REDIRECT_URL_PROPERTY_NAME;
import static no.nav.dialogarena.aktor.AktorConfig.AKTOER_ENDPOINT_URL;
import static no.nav.fo.veilarbarena.config.ApplicationConfig.*;
import static no.nav.sbl.dialogarena.common.abac.pep.CredentialConstants.SYSTEMUSER_PASSWORD;
import static no.nav.sbl.dialogarena.common.abac.pep.CredentialConstants.SYSTEMUSER_USERNAME;
import static no.nav.sbl.dialogarena.common.abac.pep.service.AbacServiceConfig.ABAC_ENDPOINT_URL_PROPERTY_NAME;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

public class Main {
    public static void main(String... args) {
        System.setProperty(SYSTEMUSER_USERNAME, getRequiredProperty("SRVVEILARBARENA_USERNAME"));
        System.setProperty(SYSTEMUSER_PASSWORD, getRequiredProperty("SRVVEILARBARENA_PASSWORD"));
        System.setProperty(StsSecurityConstants.SYSTEMUSER_USERNAME, getRequiredProperty("SRVVEILARBARENA_USERNAME"));
        System.setProperty(StsSecurityConstants.SYSTEMUSER_PASSWORD, getRequiredProperty("SRVVEILARBARENA_PASSWORD"));
        System.setProperty(StsSecurityConstants.STS_URL_KEY, getRequiredProperty(SECURITYTOKENSERVICE_URL));
        System.setProperty(AKTOER_ENDPOINT_URL, getRequiredProperty(AKTOER_V2_ENDPOINTURL));
        System.setProperty(OIDC_REDIRECT_URL_PROPERTY_NAME, getRequiredProperty(REDIRECT_URL_PROPERTY));
        System.setProperty(ABAC_ENDPOINT_URL_PROPERTY_NAME, getRequiredProperty(ABAC_PDP_ENDPOINT_URL));

        MigrationUtils.createTables(DbConfig.getDataSource());

        ApiApp.runApp(ApplicationConfig.class, args);
    }
}
