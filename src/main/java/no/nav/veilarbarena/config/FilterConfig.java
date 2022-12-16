package no.nav.veilarbarena.config;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.common.auth.context.UserRole;
import no.nav.common.auth.oidc.filter.AzureAdUserRoleResolver;
import no.nav.common.auth.oidc.filter.OidcAuthenticationFilter;
import no.nav.common.auth.oidc.filter.OidcAuthenticatorConfig;
import no.nav.common.auth.utils.ServiceUserTokenFinder;
import no.nav.common.auth.utils.UserTokenFinder;
import no.nav.common.rest.filter.ConsumerIdComplianceFilter;
import no.nav.common.rest.filter.LogRequestFilter;
import no.nav.common.rest.filter.SetStandardHttpHeadersFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static no.nav.common.auth.Constants.*;
import static no.nav.common.auth.oidc.filter.OidcAuthenticator.fromConfigs;
import static no.nav.common.utils.EnvironmentUtils.isDevelopment;
import static no.nav.common.utils.EnvironmentUtils.requireApplicationName;
import static no.nav.veilarbarena.controller.AdminController.PTO_ADMIN_SERVICE_USER;

@Configuration
public class FilterConfig {

    private final List<String> ALLOWED_SERVICE_USERS = List.of(
            "srvveilarboppfolging", "srvtiltaksgjennomf", "srvdokumentfordeling", PTO_ADMIN_SERVICE_USER
    );

    private OidcAuthenticatorConfig openAmStsAuthConfig(EnvironmentProperties properties) {
        return new OidcAuthenticatorConfig()
                .withDiscoveryUrl(properties.getOpenAmDiscoveryUrl())
                .withClientId(properties.getVeilarbloginOpenAmClientId())
                .withIdTokenFinder(new ServiceUserTokenFinder())
                .withUserRole(UserRole.SYSTEM);
    }

    private OidcAuthenticatorConfig naisStsAuthConfig(EnvironmentProperties properties) {
        return new OidcAuthenticatorConfig()
                .withDiscoveryUrl(properties.getNaisStsDiscoveryUrl())
                .withClientIds(ALLOWED_SERVICE_USERS)
                .withUserRole(UserRole.SYSTEM);
    }

    private OidcAuthenticatorConfig loginserviceIdportenConfig(EnvironmentProperties environmentProperties) {
        return new OidcAuthenticatorConfig()
                .withDiscoveryUrl(environmentProperties.getLoginserviceIdportenDiscoveryUrl())
                .withClientId(environmentProperties.getLoginserviceIdportenAudience())
                .withIdTokenCookieName(AZURE_AD_B2C_ID_TOKEN_COOKIE_NAME)
                .withUserRole(UserRole.EKSTERN);
    }

    private OidcAuthenticatorConfig naisAzureAdConfig(EnvironmentProperties properties) {
        return new OidcAuthenticatorConfig()
                .withDiscoveryUrl(properties.getNaisAadDiscoveryUrl())
                .withClientId(properties.getNaisAadClientId())
                .withUserRoleResolver(new AzureAdUserRoleResolver());
    }

    @Bean
    public FilterRegistrationBean<LogRequestFilter> logFilterRegistrationBean() {
        FilterRegistrationBean<LogRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new LogRequestFilter(requireApplicationName(), isDevelopment().orElse(false)));
        registration.setOrder(1);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<OidcAuthenticationFilter> authenticationFilterRegistrationBean(EnvironmentProperties properties) {
        FilterRegistrationBean<OidcAuthenticationFilter> registration = new FilterRegistrationBean<>();
        OidcAuthenticationFilter authenticationFilter = new OidcAuthenticationFilter(
                fromConfigs(
                        loginserviceIdportenConfig(properties),
                        openAmStsAuthConfig(properties),
                        naisStsAuthConfig(properties),
                        naisAzureAdConfig(properties)
                )
        );

        registration.setFilter(authenticationFilter);
        registration.setOrder(2);
        registration.addUrlPatterns("/api/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<ConsumerIdComplianceFilter> consumerIdComplianceFilterRegistrationBean() {
        FilterRegistrationBean<ConsumerIdComplianceFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ConsumerIdComplianceFilter(isDevelopment().orElse(false)));
        registration.setOrder(3);
        registration.addUrlPatterns("/api/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<SetStandardHttpHeadersFilter> setStandardHeadersFilterRegistrationBean() {
        FilterRegistrationBean<SetStandardHttpHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SetStandardHttpHeadersFilter());
        registration.setOrder(4);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AuthInfoFilter> authInfoFilterRegistrationBean(MeterRegistry meterRegistry) {
        FilterRegistrationBean<AuthInfoFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AuthInfoFilter(meterRegistry));
        registration.setOrder(5);
        registration.addUrlPatterns("/api/*");
        return registration;
    }
}
