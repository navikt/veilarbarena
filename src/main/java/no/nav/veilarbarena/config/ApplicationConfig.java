package no.nav.veilarbarena.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.AuthContextHolderThreadLocal;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.client.aktoroppslag.CachedAktorOppslagClient;
import no.nav.common.client.aktoroppslag.PdlAktorOppslagClient;
import no.nav.common.cxf.StsConfig;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.job.leader_election.LeaderElectionHttpClient;
import no.nav.common.metrics.InfluxClient;
import no.nav.common.metrics.MetricsClient;
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder;
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient;
import no.nav.common.utils.Credentials;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.poao_tilgang.client.AdGruppe;
import no.nav.poao_tilgang.client.Decision;
import no.nav.poao_tilgang.client.PoaoTilgangCachedClient;
import no.nav.poao_tilgang.client.PoaoTilgangClient;
import no.nav.poao_tilgang.client.PoaoTilgangHttpClient;
import no.nav.poao_tilgang.client.PolicyInput;
import no.nav.veilarbarena.client.ords.ArenaOrdsClient;
import no.nav.veilarbarena.client.ords.ArenaOrdsClientImpl;
import no.nav.veilarbarena.client.ords.ArenaOrdsTokenProviderClient;
import no.nav.veilarbarena.client.unleash.VeilarbaktivitetUnleashClient;
import no.nav.veilarbarena.client.unleash.VeilarbaktivitetUnleashClientImpl;
import no.nav.veilarbarena.client.ytelseskontrakt.YtelseskontraktClient;
import no.nav.veilarbarena.client.ytelseskontrakt.YtelseskontraktClientImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

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
	private final Cache<PolicyInput, Decision> policyInputToDecisionCache = Caffeine.newBuilder()
			.expireAfterWrite(Duration.ofMinutes(30))
			.build();
	private final Cache<UUID, List<AdGruppe>> navAnsattIdToAzureAdGrupperCache = Caffeine.newBuilder()
			.expireAfterWrite(Duration.ofMinutes(30))
			.build();
	private final Cache<String, Boolean> norskIdentToErSkjermetCache = Caffeine.newBuilder()
			.expireAfterWrite(Duration.ofMinutes(30))
			.build();

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

    @Bean VeilarbaktivitetUnleashClient veilarbaktivitetUnleashClient(EnvironmentProperties properties, AzureAdMachineToMachineTokenClient tokenClient) {
        return new VeilarbaktivitetUnleashClientImpl(properties.getVeilarbaktivitetUrl(), () -> tokenClient.createMachineToMachineToken(properties.getVeilarbaktivitetScope()));
    }

	@Bean
	public PoaoTilgangClient poaoTilgangClient(EnvironmentProperties properties, AzureAdMachineToMachineTokenClient tokenClient) {
		return new PoaoTilgangCachedClient(
			new PoaoTilgangHttpClient(
					properties.getPoaoTilgangUrl(),
					() -> tokenClient.createMachineToMachineToken(properties.getPoaoTilgangScope()),
					RestClient.baseClient()
			),
			policyInputToDecisionCache,
			navAnsattIdToAzureAdGrupperCache,
			norskIdentToErSkjermetCache
		);
	}

    private static String createArenaOrdsUrl() {
        boolean isProduction = EnvironmentUtils.isProduction().orElseThrow(() -> new IllegalStateException("Cluster name is missing"));
        return isProduction
                ? "https://arena-ords.nais.adeo.no"
                : "https://arena-ords-q2.dev.intern.nav.no";
    }

    private static String createVeilarbaktivitetUrl() {
        boolean isProduction = EnvironmentUtils.isProduction().orElseThrow(() -> new IllegalStateException("Cluster name is missing"));
        return isProduction
                ? "https://arena-ords.nais.adeo.no"
                : "https://arena-ords-q2.dev.intern.nav.no";
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
