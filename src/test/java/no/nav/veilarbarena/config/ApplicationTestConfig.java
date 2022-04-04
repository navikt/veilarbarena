package no.nav.veilarbarena.config;

import no.finn.unleash.UnleashContext;
import no.nav.common.abac.AbacClient;
import no.nav.common.abac.Pep;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.AuthContextHolderThreadLocal;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.featuretoggle.UnleashClient;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.kafka.util.KafkaPropertiesBuilder;
import no.nav.common.metrics.MetricsClient;
import no.nav.common.types.identer.Fnr;
import no.nav.common.utils.Credentials;
import no.nav.veilarbarena.client.ords.ArenaOrdsClient;
import no.nav.veilarbarena.client.ytelseskontrakt.YtelseskontraktClient;
import no.nav.veilarbarena.client.ytelseskontrakt.YtelseskontraktResponse;
import no.nav.veilarbarena.mock.AbacClientMock;
import no.nav.veilarbarena.mock.AktorregisterClientMock;
import no.nav.veilarbarena.mock.MetricsClientMock;
import no.nav.veilarbarena.mock.PepMock;
import no.nav.veilarbarena.utils.LocalH2Database;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Optional;
import java.util.Properties;

import static no.nav.veilarbarena.config.KafkaConfig.PRODUCER_CLIENT_ID;

@Configuration
@EnableConfigurationProperties({EnvironmentProperties.class})
@Import({
        ControllerTestConfig.class,
        RepositoryTestConfig.class,
        ServiceTestConfig.class,
        KafkaConfig.class,
        FilterTestConfig.class,
        HelsesjekkConfig.class
})
public class ApplicationTestConfig {

    public static final String KAFKA_IMAGE = "confluentinc/cp-kafka:5.4.3";

    @Bean
    public AuthContextHolder authContextHolder() {
        return AuthContextHolderThreadLocal.instance();
    }

    @Bean
    public Credentials serviceUserCredentials() {
        return new Credentials("username", "password");
    }

    @Bean
    public AktorOppslagClient aktorOppslagClient() {
        return new AktorregisterClientMock();
    }

    @Bean
    public AbacClient abacClient() {
        return new AbacClientMock();
    }

    @Bean
    public LeaderElectionClient leaderElectionClient() {
        return () -> true;
    }

    @Bean
    public Pep veilarbPep(AbacClient abacClient) {
        return new PepMock(abacClient);
    }

    @Bean
    public MetricsClient metricsClient() {
        return new MetricsClientMock();
    }

    @Bean
    public KafkaContainer kafkaContainer() {
        KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE));
        kafkaContainer.start();
        return kafkaContainer;
    }

    @Bean
    public KafkaConfig.EnvironmentContext kafkaConfigEnvironmentContext(KafkaContainer kafkaContainer) {
        Properties properties = KafkaPropertiesBuilder.producerBuilder()
                .withBrokerUrl(kafkaContainer.getBootstrapServers())
                .withProducerId(PRODUCER_CLIENT_ID)
                .withSerializers(ByteArraySerializer.class, ByteArraySerializer.class)
                .build();

        return new KafkaConfig.EnvironmentContext()
                .setAivenProducerClientProperties(properties)
                .setOnPremProducerClientProperties(properties);
    }

    @Bean
    public DataSource dataSource() {
        return LocalH2Database.getDb().getDataSource();
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return LocalH2Database.getDb();
    }

    @Bean
    public ArenaOrdsClient arenaOrdsClient() {
        return new ArenaOrdsClient() {
            @Override
            public <T> Optional<T> get(String path, String fnr, Class<T> clazz) {
                return Optional.empty();
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }
        };
    }

    @Bean
    public UnleashClient unleashClient() {
        return new UnleashClient() {
            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }

            @Override
            public boolean isEnabled(String s) {
                return true;
            }

            @Override
            public boolean isEnabled(String s, UnleashContext unleashContext) {
                return true;
            }
        };
    }

    @Bean
    public YtelseskontraktClient ytelseskontraktClient() {
        return new YtelseskontraktClient() {
            @Override
            public YtelseskontraktResponse hentYtelseskontraktListe(Fnr personId, XMLGregorianCalendar periodeFom, XMLGregorianCalendar periodeTom) {
                return null;
            }

            @Override
            public YtelseskontraktResponse hentYtelseskontraktListe(Fnr personId) {
                return null;
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }
        };
    }

}
