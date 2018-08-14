import no.nav.brukerdialog.security.Constants;
import no.nav.brukerdialog.tools.SecurityConstants;
import no.nav.dialogarena.config.fasit.FasitUtils;
import no.nav.dialogarena.config.fasit.ServiceUser;
import no.nav.fo.veilarbarena.DatabaseTestContext;
import no.nav.testconfig.ApiAppTest;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static no.nav.fo.veilarbarena.config.ApplicationConfig.APPLICATION_NAME;
import static no.nav.fo.veilarbarena.config.ApplicationConfig.REDIRECT_URL_PROPERTY;

public class MainTest {
    private static final String TEST_PORT = "8790";

    public static void main(String[] args) throws Exception {
        setProperty("SERVICE_CALLS_HOME", "target/log");

        ApiAppTest.setupTestContext(ApiAppTest.Config.builder().applicationName(APPLICATION_NAME).build());
        DatabaseTestContext.setupContext(getProperty("database", "T6"));

// TODO finne løsning for lokal-kjøring
//        setupSecurity();
        String loginUrl = FasitUtils.getBaseUrl("veilarblogin.redirect-url", FasitUtils.Zone.FSS);
        setProperty(REDIRECT_URL_PROPERTY, loginUrl);
        Main.main(TEST_PORT);
    }

    public static void setupSecurity(){
        String issoHost = FasitUtils.getBaseUrl("isso-host");
        String issoJWS = FasitUtils.getBaseUrl("isso-jwks");
        String issoISSUER = FasitUtils.getBaseUrl("isso-issuer");
        String issoIsAlive = FasitUtils.getBaseUrl("isso.isalive", FasitUtils.Zone.FSS);
        ServiceUser srvveilarbarena = FasitUtils.getServiceUser("srvveilarena", APPLICATION_NAME);
        ServiceUser isso_rp_user = FasitUtils.getServiceUser("isso-rp-user", APPLICATION_NAME);

        setProperty(Constants.ISSO_HOST_URL_PROPERTY_NAME, issoHost);
        setProperty(Constants.ISSO_RP_USER_USERNAME_PROPERTY_NAME, isso_rp_user.getUsername());
        setProperty(Constants.ISSO_RP_USER_PASSWORD_PROPERTY_NAME, isso_rp_user.getPassword());
        setProperty(Constants.ISSO_JWKS_URL_PROPERTY_NAME, issoJWS);
        setProperty(Constants.ISSO_ISSUER_URL_PROPERTY_NAME, issoISSUER);
        setProperty(Constants.ISSO_ISALIVE_URL_PROPERTY_NAME, issoIsAlive);
        setProperty(SecurityConstants.SYSTEMUSER_USERNAME, srvveilarbarena.getUsername());
        setProperty(SecurityConstants.SYSTEMUSER_PASSWORD, srvveilarbarena.getPassword());
    }
}
