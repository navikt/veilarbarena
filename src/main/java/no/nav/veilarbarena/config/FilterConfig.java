package no.nav.veilarbarena.config;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.common.auth.context.UserRole;
import no.nav.common.auth.oidc.filter.AzureAdUserRoleResolver;
import no.nav.common.auth.oidc.filter.JavaxOidcAuthenticationFilter;
import no.nav.common.auth.oidc.filter.JavaxOidcAuthenticatorConfig;
import no.nav.common.rest.filter.*;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static no.nav.common.auth.Constants.*;
import static no.nav.common.auth.oidc.filter.JavaxOidcAuthenticator.fromConfigs;
import static no.nav.common.utils.EnvironmentUtils.isDevelopment;
import static no.nav.common.utils.EnvironmentUtils.requireApplicationName;
import static no.nav.veilarbarena.controller.AdminController.PTO_ADMIN_SERVICE_USER;

@Configuration
public class FilterConfig {

    private final List<String> ALLOWED_SERVICE_USERS = List.of(
            "srvveilarboppfolging", "srvtiltaksgjennomf", "srvdokumentfordeling", PTO_ADMIN_SERVICE_USER
    );

    private JavaxOidcAuthenticatorConfig naisStsAuthConfig(EnvironmentProperties properties) {
        return new JavaxOidcAuthenticatorConfig()
                .withDiscoveryUrl(properties.getNaisStsDiscoveryUrl())
                .withClientIds(ALLOWED_SERVICE_USERS)
                .withUserRole(UserRole.SYSTEM);
    }

    private JavaxOidcAuthenticatorConfig loginserviceIdportenConfig(EnvironmentProperties environmentProperties) {
        return new JavaxOidcAuthenticatorConfig()
                .withDiscoveryUrl(environmentProperties.getLoginserviceIdportenDiscoveryUrl())
                .withClientId(environmentProperties.getLoginserviceIdportenAudience())
                .withIdTokenCookieName(AZURE_AD_B2C_ID_TOKEN_COOKIE_NAME)
                .withUserRole(UserRole.EKSTERN);
    }

    private JavaxOidcAuthenticatorConfig naisAzureAdConfig(EnvironmentProperties properties) {
        return new JavaxOidcAuthenticatorConfig()
                .withDiscoveryUrl(properties.getNaisAadDiscoveryUrl())
                .withClientId(properties.getNaisAadClientId())
                .withUserRoleResolver(new AzureAdUserRoleResolver());
    }

    @Bean
    public FilterRegistrationBean<JavaxLogRequestFilter> logFilterRegistrationBean() {
        FilterRegistrationBean<JavaxLogRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JavaxLogRequestFilter(requireApplicationName(), isDevelopment().orElse(false)));
        registration.setOrder(1);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<JavaxOidcAuthenticationFilter> authenticationFilterRegistrationBean(EnvironmentProperties properties) {
        FilterRegistrationBean<JavaxOidcAuthenticationFilter> registration = new FilterRegistrationBean<>();
        JavaxOidcAuthenticationFilter authenticationFilter = new JavaxOidcAuthenticationFilter(
                fromConfigs(
                        loginserviceIdportenConfig(properties),
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
    public FilterRegistrationBean<JavaxConsumerIdComplianceFilter> consumerIdComplianceFilterRegistrationBean() {
        FilterRegistrationBean<JavaxConsumerIdComplianceFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JavaxConsumerIdComplianceFilter(isDevelopment().orElse(false)));
        registration.setOrder(3);
        registration.addUrlPatterns("/api/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<JavaxSetStandardHttpHeadersFilter> setStandardHeadersFilterRegistrationBean() {
        FilterRegistrationBean<JavaxSetStandardHttpHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JavaxSetStandardHttpHeadersFilter());
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
