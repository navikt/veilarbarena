package no.nav.fo.veilarbarena.config;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.brukerdialog.security.oidc.provider.SecurityTokenServiceOidcProvider;
import no.nav.brukerdialog.security.oidc.provider.SecurityTokenServiceOidcProviderConfig;
import no.nav.fo.veilarbarena.service.AktoerRegisterService;
import no.nav.fo.veilarbarena.client.RestClientConfig;
import no.nav.fo.veilarbarena.scheduled.UserChangePublisher;
import no.nav.fo.veilarbarena.service.InternalServlet;
import no.nav.fo.veilarbarena.service.OppfolgingsbrukerController;
import no.nav.sbl.featuretoggle.unleash.UnleashService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import static no.nav.apiapp.ServletUtil.leggTilServlet;

import static no.nav.brukerdialog.security.oidc.provider.SecurityTokenServiceOidcProviderConfig.STS_OIDC_CONFIGURATION_URL_PROPERTY;
import static no.nav.sbl.featuretoggle.unleash.UnleashServiceConfig.resolveFromEnvironment;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Configuration
@Import({
        OppfolgingsbrukerController.class,
        DbConfig.class,
        CacheConfig.class,
        PepConfig.class,
        ServiceConfig.class,
        UserChangeConfig.class,
        InternalServlet.class,
        AktoerRegisterService.class,
        RestClientConfig.class
})

@EnableAspectJAutoProxy(proxyTargetClass = true)
public class ApplicationConfig implements ApiApplication {
    public static final String APPLICATION_NAME = "veilarbarena";
    public static final String AKTOER_V2_ENDPOINTURL = "AKTOER_V2_ENDPOINTURL";
    public static final String REDIRECT_URL_PROPERTY = "VEILARBLOGIN_REDIRECT_URL_URL";
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

        SecurityTokenServiceOidcProvider securityTokenServiceOidcProvider = new SecurityTokenServiceOidcProvider(SecurityTokenServiceOidcProviderConfig.builder()
                .discoveryUrl(getRequiredProperty(STS_OIDC_CONFIGURATION_URL_PROPERTY))
                .build());

        apiAppConfigurator
                .issoLogin()
                .sts()
                .validateAzureAdInternalUsersTokens()
                .oidcProvider(securityTokenServiceOidcProvider);
    }

    @Bean
    public UnleashService unleashService() {
        return new UnleashService(resolveFromEnvironment());
    }
}
