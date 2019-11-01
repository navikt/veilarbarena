package no.nav.fo.veilarbarena.config;

import no.nav.brukerdialog.security.Constants;
import no.nav.fasit.FasitUtils;
import no.nav.fasit.ServiceUser;
import no.nav.fasit.dto.RestService;
import no.nav.testconfig.ApiAppTest;

import static java.lang.System.setProperty;
import static no.nav.brukerdialog.security.Constants.OIDC_REDIRECT_URL_PROPERTY_NAME;
import static no.nav.fo.veilarbarena.config.ApplicationConfig.AKTOERREGISTER_API_V1_URL;
import static no.nav.fo.veilarbarena.config.ApplicationConfig.APPLICATION_NAME;

public class ApplicationTestConfig {

    public static void setupTestConfig() {
        String issoHost = FasitUtils.getBaseUrl("isso-host");
        ServiceUser isso_rp_user = FasitUtils.getServiceUser("isso-rp-user", APPLICATION_NAME);
        String issoJWS = FasitUtils.getBaseUrl("isso-jwks");
        String issoISSUER = FasitUtils.getBaseUrl("isso-issuer");
        RestService aktoerregisterService = FasitUtils.getRestService("aktoerregister.api.v1", FasitUtils.getDefaultEnvironment());

        ApiAppTest.setupTestContext(ApiAppTest.Config.builder().applicationName(APPLICATION_NAME).build());
        setProperty("no.nav.modig.security.systemuser.username", "SRVVEILARBARENA");
        setProperty("no.nav.modig.security.systemuser.password", "PASSORD");
        setProperty(AKTOERREGISTER_API_V1_URL, aktoerregisterService.getUrl());
        setProperty(Constants.ISSO_HOST_URL_PROPERTY_NAME, issoHost);
        setProperty(Constants.ISSO_RP_USER_USERNAME_PROPERTY_NAME, isso_rp_user.getUsername());
        setProperty(Constants.ISSO_RP_USER_PASSWORD_PROPERTY_NAME, isso_rp_user.getPassword());
        setProperty(Constants.ISSO_JWKS_URL_PROPERTY_NAME, issoJWS);
        setProperty(Constants.ISSO_ISSUER_URL_PROPERTY_NAME, issoISSUER);
        setProperty(OIDC_REDIRECT_URL_PROPERTY_NAME, FasitUtils.getRestService("veilarblogin.redirect-url", FasitUtils.getDefaultEnvironment()).getUrl());
    }
}
