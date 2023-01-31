package no.nav.veilarbarena.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.abac.Pep;
import no.nav.common.abac.VeilarbPepFactory;
import no.nav.common.abac.audit.SpringAuditRequestInfoSupplier;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.AuthContextHolderThreadLocal;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.client.aktoroppslag.CachedAktorOppslagClient;
import no.nav.common.client.aktoroppslag.PdlAktorOppslagClient;
import no.nav.common.cxf.StsConfig;
import no.nav.common.featuretoggle.UnleashClient;
import no.nav.common.featuretoggle.UnleashClientImpl;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.job.leader_election.LeaderElectionHttpClient;
import no.nav.common.metrics.InfluxClient;
import no.nav.common.metrics.MetricsClient;
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder;
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient;
import no.nav.common.utils.Credentials;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.veilarbarena.client.ords.ArenaOrdsClient;
import no.nav.veilarbarena.client.ords.ArenaOrdsClientImpl;
import no.nav.veilarbarena.client.ords.ArenaOrdsTokenProviderClient;
import no.nav.veilarbarena.client.ytelseskontrakt.YtelseskontraktClient;
import no.nav.veilarbarena.client.ytelseskontrakt.YtelseskontraktClientImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import static no.nav.common.kafka.util.KafkaPropertiesPreset.aivenByteProducerProperties;
import static no.nav.common.utils.NaisUtils.getCredentials;
import static no.nav.common.utils.UrlUtils.createDevInternalIngressUrl;
import static no.nav.common.utils.UrlUtils.createProdInternalIngressUrl;
import static no.nav.veilarbarena.config.KafkaConfig.PRODUCER_CLIENT_ID;


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
    public AzureAdMachineToMachineTokenClient azureAdMachineToMachineTokenClient() {
        return AzureAdTokenClientBuilder.builder()
                .withNaisDefaults()
                .buildMachineToMachineTokenClient();
    }

    @Bean
    public UnleashClient unleashClient(EnvironmentProperties properties) {
        return new UnleashClientImpl(properties.getUnleashUrl(), APPLICATION_NAME);
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
    public AuthContextHolder authContextHolder() {
        return AuthContextHolderThreadLocal.instance();
    }

    @Bean
    public AktorOppslagClient aktorOppslagClient(AzureAdMachineToMachineTokenClient tokenClient) {
        String tokenScope = String.format("api://%s.pdl.pdl-api/.default",
                isProduction() ? "prod-fss" : "dev-fss");

        AktorOppslagClient aktorOppslagClient = new PdlAktorOppslagClient(
                internalDevOrProdPdlIngress(),
                () -> tokenClient.createMachineToMachineToken(tokenScope)
        );
        return new CachedAktorOppslagClient(aktorOppslagClient);
    }

    @Bean
    public KafkaConfig.EnvironmentContext kafkaConfigEnvContext() {
        return new KafkaConfig.EnvironmentContext()
                .setProducerClientProperties(aivenByteProducerProperties(PRODUCER_CLIENT_ID));
    }

    @Bean
    public Pep veilarbPep(EnvironmentProperties properties, Credentials serviceUserCredentials) {
        return VeilarbPepFactory.get(
                properties.getAbacUrl(), serviceUserCredentials.username,
                serviceUserCredentials.password, new SpringAuditRequestInfoSupplier()
        );
    }

    @Bean
    public static StsConfig stsConfig(EnvironmentProperties properties, Credentials serviceUserCredentials) {
        return StsConfig.builder()
                .url(properties.getSoapStsUrl())
                .username(serviceUserCredentials.username)
                .password(serviceUserCredentials.password)
                .build();
    }

    @Bean
    public YtelseskontraktClient ytelseskontraktClient(EnvironmentProperties properties, StsConfig stsConfig) {
        return new YtelseskontraktClientImpl(properties.getYtelseskontraktV3Endpoint(), stsConfig);
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
                ? "https://arena-ords.nais.adeo.no"
                : "https://arena-ords-q1.dev.intern.nav.no";
    }

    private String internalDevOrProdPdlIngress() {
        return isProduction()
                ? createProdInternalIngressUrl("pdl-api")
                : createDevInternalIngressUrl("pdl-api");
    }

    private static boolean isProduction() {
        return EnvironmentUtils.isProduction().orElseThrow();
    }
}
