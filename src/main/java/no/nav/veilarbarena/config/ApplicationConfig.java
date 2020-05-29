package no.nav.veilarbarena.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.abac.*;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.client.aktorregister.AktorregisterHttpClient;
import no.nav.common.client.aktorregister.CachedAktorregisterClient;
import no.nav.common.featuretoggle.UnleashService;
import no.nav.common.metrics.InfluxClient;
import no.nav.common.metrics.MetricsClient;
import no.nav.common.metrics.SensuConfig;
import no.nav.common.sts.NaisSystemUserTokenProvider;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.common.utils.Credentials;
import no.nav.veilarbarena.service.ArenaOrdsService;
import no.nav.veilarbarena.utils.ArenaOrdsTokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import static no.nav.common.featuretoggle.UnleashServiceConfig.resolveFromEnvironment;
import static no.nav.common.utils.NaisUtils.getCredentials;


@Slf4j
@Configuration
@EnableScheduling
@EnableConfigurationProperties({EnvironmentProperties.class})
public class ApplicationConfig {

    public static final String APPLICATION_NAME = "veilarbarena";
    public static final String AKTOER_V2_ENDPOINTURL = "AKTOER_V2_ENDPOINTURL";
    public static final String REDIRECT_URL_PROPERTY = "VEILARBLOGIN_REDIRECT_URL_URL";
    public static final String SECURITYTOKENSERVICE_URL = "SECURITYTOKENSERVICE_URL";
    public static final String ABAC_PDP_ENDPOINT_URL = "ABAC_PDP_ENDPOINT_URL";
    public static final String AKTOERREGISTER_API_V1_URL = "AKTOERREGISTER_API_V1_URL";

    @Bean
    public Credentials serviceUserCredentials() {
        return getCredentials("service_user");
    }

    @Bean
    public ArenaOrdsService arenaOrdsService() {
        return new ArenaOrdsService(new ArenaOrdsTokenProvider());
    }

    @Bean
    public UnleashService unleashService() {
        return new UnleashService(resolveFromEnvironment());
    }

    @Bean
    public SystemUserTokenProvider systemUserTokenProvider(EnvironmentProperties properties, Credentials serviceUserCredentials) {
        return new NaisSystemUserTokenProvider(properties.getStsDiscoveryUrl(), serviceUserCredentials.username, serviceUserCredentials.password);
    }

    @Bean
    public AktorregisterClient aktorregisterClient(EnvironmentProperties properties, SystemUserTokenProvider tokenProvider) {
        AktorregisterClient aktorregisterClient = new AktorregisterHttpClient(
                properties.getAktorregisterUrl(), APPLICATION_NAME, tokenProvider::getSystemUserToken
        );
        return new CachedAktorregisterClient(aktorregisterClient);
    }

    @Bean
    public AbacClient abacClient(EnvironmentProperties properties, Credentials serviceUserCredentials) {
        return new AbacCachedClient(new AbacHttpClient(properties.getAbacUrl(), serviceUserCredentials.username, serviceUserCredentials.password));
    }

    @Bean
    public Pep veilarbPep(Credentials serviceUserCredentials, AbacClient abacClient) {
        return new VeilarbPep(serviceUserCredentials.username, abacClient, new AuditLogger());
    }

}
