package no.nav.veilarbarena.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.abac.*;
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
import no.nav.veilarbarena.client.ArenaOrdsClient;
import no.nav.veilarbarena.client.ArenaOrdsClientImpl;
import no.nav.veilarbarena.client.ArenaOrdsTokenProviderClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import static no.nav.common.featuretoggle.UnleashServiceConfig.resolveFromEnvironment;
import static no.nav.common.utils.NaisUtils.getCredentials;
import static no.nav.common.utils.UrlUtils.clusterUrlForApplication;


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
    public LeaderElectionClient leaderElectionClient() {
        return new LeaderElectionHttpClient();
    }

    @Bean
    public MetricsClient metricsClient() {
        return new InfluxClient();
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

    @Bean
    public ArenaOrdsTokenProviderClient arenaOrdsTokenProvider(EnvironmentProperties properties) {
        return new ArenaOrdsTokenProviderClient(getArenaOrdsUrl(properties));
    }

    @Bean
    public ArenaOrdsClient arenaOrdsClient(EnvironmentProperties properties, ArenaOrdsTokenProviderClient arenaOrdsTokenProviderClient) {
        return new ArenaOrdsClientImpl(getArenaOrdsUrl(properties), arenaOrdsTokenProviderClient::getToken);
    }

    private static String getArenaOrdsUrl(EnvironmentProperties properties) {
        String propertiesUrl = properties.getArenaOrdsUrl();
        return propertiesUrl != null
                ? propertiesUrl
                : clusterUrlForApplication("arena-ords");
    }

}
