package no.nav.fo.veilarbarena.config;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.fo.veilarbarena.client.AktoerRegisterClient;
import no.nav.fo.veilarbarena.client.RestClientConfig;
import no.nav.fo.veilarbarena.scheduled.UserChangePublisher;
import no.nav.fo.veilarbarena.service.InternalServlet;
import no.nav.fo.veilarbarena.service.OppfolgingsbrukerController;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import static no.nav.apiapp.ServletUtil.leggTilServlet;

@Configuration
@Import({
        OppfolgingsbrukerController.class,
        DbConfig.class,
        CacheConfig.class,
        ServiceConfig.class,
        UserChangeConfig.class,
        InternalServlet.class,
        AktoerRegisterClient.class,
        RestClientConfig.class
})

@EnableAspectJAutoProxy(proxyTargetClass = true)
public class ApplicationConfig implements ApiApplication {
    public static final String APPLICATION_NAME = "veilarbarena";
    public static final String AKTOER_V2_ENDPOINTURL = "AKTOER_V2_ENDPOINTURL";
    public static final String REDIRECT_URL_PROPERTY = "VEILARBLOGIN_REDIRECT_URL";
    public static final String SECURITYTOKENSERVICE_URL = "SECURITYTOKENSERVICE_URL";
    public static final String ABAC_PDP_ENDPOINT_URL = "ABAC_PDP_ENDPOINT_URL";
    public static final String AKTOERREGISTER_API_V1_URL = "AKTOERREGISTER_API_V1_URL";

    @Inject
    UserChangePublisher userChangePublisher;

    @Override
    public void startup(ServletContext servletContext) {
        leggTilServlet(servletContext, new InternalServlet(userChangePublisher), "/internal/publiser_alle_brukere");
    }

    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {
        apiAppConfigurator.issoLogin();
        apiAppConfigurator.sts();
    }
}
