import no.nav.brukerdialog.security.Constants;
import no.nav.dialogarena.config.fasit.FasitUtils;
import no.nav.dialogarena.config.fasit.ServiceUser;
import no.nav.dialogarena.config.fasit.dto.RestService;
import no.nav.fo.veilarbarena.DatabaseTestContext;
import no.nav.fo.veilarbarena.config.ApplicationConfig;
import no.nav.fo.veilarbarena.soapproxy.oppfolgingstatus.OppfolgingstatusConfig;
import no.nav.sbl.dialogarena.common.abac.pep.CredentialConstants;
import no.nav.testconfig.ApiAppTest;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static no.nav.dialogarena.config.fasit.FasitUtils.Zone.FSS;
import static no.nav.fo.veilarbarena.config.ApplicationConfig.*;
import static no.nav.sbl.util.EnvironmentUtils.requireEnvironmentName;

public class MainTest {
    private static final String TEST_PORT = "8790";

    public static void main(String[] args) {
        ApiAppTest.setupTestContext(ApiAppTest.Config.builder().applicationName(APPLICATION_NAME).build());
        DatabaseTestContext.setupContext(getProperty("database", "Q6"));
        setupSecurity();

        Main.main(TEST_PORT);
    }

    private static void setupSecurity() {
        String issoHost = FasitUtils.getBaseUrl("isso-host");
        String issoJWS = FasitUtils.getBaseUrl("isso-jwks");
        String issoISSUER = FasitUtils.getBaseUrl("isso-issuer");
        String issoIsAlive = FasitUtils.getBaseUrl("isso.isalive", FSS);
        ServiceUser srvveilarbarena = FasitUtils.getServiceUser("srvveilarbarena", APPLICATION_NAME);
        ServiceUser isso_rp_user = FasitUtils.getServiceUser("isso-rp-user", APPLICATION_NAME);
        String securityTokenService = FasitUtils.getBaseUrl("securityTokenService", FSS);
        RestService redirectUrlService = FasitUtils.getRestService("veilarblogin.redirect-url", FasitUtils.getDefaultEnvironment());
        RestService abac = FasitUtils.getRestService("abac.pdp.endpoint", FasitUtils.getDefaultEnvironment());
        String endringBrukerTopic = "aapen-fo-endringPaaOppfoelgingsBruker-v1-"+ requireEnvironmentName();
        String kafkaBrokers = FasitUtils.getBaseUrl("kafka-brokers");

        setProperty("ENDRING_BRUKER_TOPIC", endringBrukerTopic);
        setProperty("KAFKA_BROKERS_URL", kafkaBrokers);
        setProperty("SRVVEILARBARENA_USERNAME", srvveilarbarena.getUsername());
        setProperty("SRVVEILARBARENA_PASSWORD", srvveilarbarena.getPassword());
        setProperty(SECURITYTOKENSERVICE_URL, securityTokenService);
        setProperty(ABAC_PDP_ENDPOINT_URL, abac.getUrl());

        setProperty(CredentialConstants.SYSTEMUSER_USERNAME, srvveilarbarena.getUsername());
        setProperty(CredentialConstants.SYSTEMUSER_PASSWORD, srvveilarbarena.getPassword());
        setProperty(Constants.ISSO_HOST_URL_PROPERTY_NAME, issoHost);
        setProperty(Constants.ISSO_RP_USER_USERNAME_PROPERTY_NAME, isso_rp_user.getUsername());
        setProperty(Constants.ISSO_RP_USER_PASSWORD_PROPERTY_NAME, isso_rp_user.getPassword());
        setProperty(Constants.ISSO_JWKS_URL_PROPERTY_NAME, issoJWS);
        setProperty(Constants.ISSO_ISSUER_URL_PROPERTY_NAME, issoISSUER);
        setProperty(Constants.ISSO_ISALIVE_URL_PROPERTY_NAME, issoIsAlive);
        setProperty(ApplicationConfig.REDIRECT_URL_PROPERTY, redirectUrlService.getUrl());

        setProperty(AKTOER_V2_ENDPOINTURL, FasitUtils.getWebServiceEndpoint("Aktoer_v2").getUrl());
        setProperty(OppfolgingstatusConfig.ENDPOINTURL, FasitUtils.getWebServiceEndpoint("virksomhet:Oppfoelgingsstatus_v2").getUrl());
    }
}
