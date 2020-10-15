package no.nav.veilarbarena.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.abac.Pep;
import no.nav.common.abac.VeilarbPep;
import no.nav.common.abac.audit.SpringAuditRequestInfoSupplier;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.client.aktorregister.AktorregisterHttpClient;
import no.nav.common.client.aktorregister.CachedAktorregisterClient;
import no.nav.common.featuretoggle.UnleashService;
import no.nav.common.leaderelection.LeaderElectionClient;
import no.nav.common.leaderelection.LeaderElectionHttpClient;
import no.nav.common.metrics.InfluxClient;
import no.nav.common.metrics.MetricsClient;
import no.nav.common.sts.NaisSystemUserTokenProvider;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.common.utils.Credentials;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.veilarbarena.client.ArenaOrdsClient;
import no.nav.veilarbarena.client.ArenaOrdsClientImpl;
import no.nav.veilarbarena.client.ArenaOrdsTokenProviderClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import static no.nav.common.featuretoggle.UnleashServiceConfig.resolveFromEnvironment;
import static no.nav.common.utils.NaisUtils.getCredentials;
import static no.nav.common.utils.UrlUtils.createServiceUrl;


@Slf4j
@Configuration
@EnableScheduling
@EnableConfigurationProperties({EnvironmentProperties.class})
public class ApplicationConfig {

    public static final String APPLICATION_NAME = "veilarbarena";

    @Bean
    public Credentials serviceUserCredentials() {
        return getCredentials("service_user");
    }

    @Bean
    public UnleashService unleashService() {
        return new UnleashService(resolveFromEnvironment());
    }

    @Bean
    public LeaderElectionClient leaderElectionClient(UnleashService unleashService) {
        return new LeaderElectionClient() {
            private final LeaderElectionHttpClient httpClient = new LeaderElectionHttpClient();
            @Override
            public boolean isLeader() {
                boolean isNamespaceLeader = unleashService.isEnabled("veilarbarena.is_namespace_leader");
                log.info("Is namespace leader {}", isNamespaceLeader);
                return isNamespaceLeader && httpClient.isLeader();
            }
        };
    }
    @Bean
    public MetricsClient metricsClient() {
        return new InfluxClient();
    }

    @Bean
    public SystemUserTokenProvider systemUserTokenProvider(EnvironmentProperties properties, Credentials serviceUserCredentials) {
        return new NaisSystemUserTokenProvider(properties.getNaisStsDiscoveryUrl(), serviceUserCredentials.username, serviceUserCredentials.password);
    }

    @Bean
    public AktorregisterClient aktorregisterClient(EnvironmentProperties properties, SystemUserTokenProvider tokenProvider) {
        AktorregisterClient aktorregisterClient = new AktorregisterHttpClient(
                properties.getAktorregisterUrl(), APPLICATION_NAME, tokenProvider::getSystemUserToken
        );
        return new CachedAktorregisterClient(aktorregisterClient);
    }

    @Bean
    public Pep veilarbPep(EnvironmentProperties properties, Credentials serviceUserCredentials) {
        return new VeilarbPep(
                properties.getAbacUrl(), serviceUserCredentials.username,
                serviceUserCredentials.password, new SpringAuditRequestInfoSupplier()
        );
    }

    @Bean
    public ArenaOrdsTokenProviderClient arenaOrdsTokenProvider() {
        return new ArenaOrdsTokenProviderClient(createArenaOrdsUrl());
    }

    @Bean
    public ArenaOrdsClient arenaOrdsClient(ArenaOrdsTokenProviderClient arenaOrdsTokenProviderClient) {
        return new ArenaOrdsClientImpl(createArenaOrdsUrl(), arenaOrdsTokenProviderClient::getToken);
    }

    private static String createArenaOrdsUrl() {
        boolean isProduction = EnvironmentUtils.isProduction().orElseThrow(() -> new IllegalStateException("Cluster name is missing"));
        return isProduction
                ? createServiceUrl("arena-ords", "default", false)
                : createServiceUrl("arena-ords", "q1", false);
    }

}
