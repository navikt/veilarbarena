import no.nav.apiapp.ApiApp;
import no.nav.fo.veilarbarena.utils.MigrationUtils;
import no.nav.fo.veilarbarena.config.ApplicationConfig;
import no.nav.fo.veilarbarena.config.DbConfig;
import no.nav.common.utils.NaisUtils;
import no.nav.sbl.dialogarena.common.cxf.StsSecurityConstants;

import static no.nav.brukerdialog.security.Constants.OIDC_REDIRECT_URL_PROPERTY_NAME;
import static no.nav.dialogarena.aktor.AktorConfig.AKTOER_ENDPOINT_URL;
import static no.nav.fo.veilarbarena.config.ApplicationConfig.*;
import static no.nav.fo.veilarbarena.config.DbConfig.VEILARBARENADB_PASSWORD;
import static no.nav.fo.veilarbarena.config.DbConfig.VEILARBARENADB_USERNAME;
import static no.nav.common.utils.NaisUtils.getCredentials;
import static no.nav.sbl.dialogarena.common.abac.pep.CredentialConstants.SYSTEMUSER_PASSWORD;
import static no.nav.sbl.dialogarena.common.abac.pep.CredentialConstants.SYSTEMUSER_USERNAME;
import static no.nav.sbl.dialogarena.common.abac.pep.service.AbacServiceConfig.ABAC_ENDPOINT_URL_PROPERTY_NAME;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

public class Main {
    public static void main(String... args) {
        readFromConfigMap();

        NaisUtils.Credentials serviceUser = getCredentials("service_user");
        System.setProperty(SYSTEMUSER_USERNAME, serviceUser.username);
        System.setProperty(SYSTEMUSER_PASSWORD, serviceUser.password);
        System.setProperty(StsSecurityConstants.SYSTEMUSER_USERNAME, serviceUser.username);
        System.setProperty(StsSecurityConstants.SYSTEMUSER_PASSWORD, serviceUser.password);
        System.setProperty(StsSecurityConstants.STS_URL_KEY, getRequiredProperty(SECURITYTOKENSERVICE_URL));
        System.setProperty(AKTOER_ENDPOINT_URL, getRequiredProperty(AKTOER_V2_ENDPOINTURL));
        System.setProperty(OIDC_REDIRECT_URL_PROPERTY_NAME, getRequiredProperty(REDIRECT_URL_PROPERTY));
        System.setProperty(ABAC_ENDPOINT_URL_PROPERTY_NAME, getRequiredProperty(ABAC_PDP_ENDPOINT_URL));
        System.setProperty(AKTOERREGISTER_API_V1_URL, getRequiredProperty(AKTOERREGISTER_API_V1_URL));

        NaisUtils.Credentials oracleCreds = getCredentials("oracle_creds");
        System.setProperty(VEILARBARENADB_USERNAME, oracleCreds.username);
        System.setProperty(VEILARBARENADB_PASSWORD, oracleCreds.password);

        MigrationUtils.createTables(DbConfig.getDataSource());

        ApiApp.runApp(ApplicationConfig.class, args);
    }

    private static void readFromConfigMap() {
        NaisUtils.addConfigMapToEnv("pto-config",
                "APPDYNAMICS_AGENT_ACCOUNT_NAME",
                "APPDYNAMICS_CONTROLLER_HOST_NAME",
                "APPDYNAMICS_CONTROLLER_PORT",
                "APPDYNAMICS_CONTROLLER_SSL_ENABLED",
                "KAFKA_BROKERS_URL",
                "SECURITYTOKENSERVICE_URL",
                "ABAC_PDP_ENDPOINT_URL",
                "ABAC_PDP_ENDPOINT_DESCRIPTION",
                "ISSO_HOST_URL",
                "ISSO_JWKS_URL",
                "ISSO_ISSUER_URL",
                "ISSO_ISALIVE_URL",
                "VEILARBLOGIN_REDIRECT_URL_DESCRIPTION",
                "VEILARBLOGIN_REDIRECT_URL_URL",
                "AKTOER_V2_SECURITYTOKEN",
                "AKTOER_V2_ENDPOINTURL",
                "AKTOER_V2_WSDLURL",
                "VIRKSOMHET_OPPFOELGINGSSTATUS_V2_SECURITYTOKEN",
                "VIRKSOMHET_OPPFOELGINGSSTATUS_V2_ENDPOINTURL",
                "VIRKSOMHET_OPPFOELGINGSSTATUS_V2_WSDLURL",
                "LOGINSERVICE_OIDC_CALLBACKURI",
                "LOGINSERVICE_OIDC_DISCOVERYURI",
                "UNLEASH_API_URL",
                "AKTOERREGISTER_API_V1_URL"
        );
    }
}
