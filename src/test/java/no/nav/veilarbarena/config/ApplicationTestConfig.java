package no.nav.veilarbarena.config;

import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.AuthContextHolderThreadLocal;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.kafka.util.KafkaPropertiesBuilder;
import no.nav.common.metrics.MetricsClient;
import no.nav.common.types.identer.Fnr;
import no.nav.common.utils.Credentials;
import no.nav.veilarbarena.client.ords.ArenaOrdsClient;
import no.nav.veilarbarena.client.ords.dto.ArenaAktiviteterDTO;
import no.nav.veilarbarena.client.ords.dto.ArenaOppfolgingssakDTO;
import no.nav.veilarbarena.client.ords.dto.ArenaOppfolgingsstatusDTO;
import no.nav.veilarbarena.client.unleash.VeilarbaktivitetUnleashClient;
import no.nav.veilarbarena.client.ytelseskontrakt.YtelseskontraktClient;
import no.nav.veilarbarena.client.ytelseskontrakt.YtelseskontraktResponse;
import no.nav.veilarbarena.mock.MetricsClientMock;
import no.nav.veilarbarena.utils.LocalH2Database;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.mockito.Mockito;
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
        return Mockito.mock(AktorOppslagClient.class);
    }

    @Bean
    public LeaderElectionClient leaderElectionClient() {
        return () -> true;
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
                .setProducerClientProperties(properties);
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
            public Optional<ArenaOppfolgingsstatusDTO> hentArenaOppfolgingsstatus(Fnr fnr) {
                return Optional.empty();
            }

            @Override
            public Optional<ArenaOppfolgingssakDTO> hentArenaOppfolgingssak(Fnr fnr) {
                return Optional.empty();
            }

            @Override
            public Optional<ArenaAktiviteterDTO> hentArenaAktiviteter(Fnr fnr) {
                return Optional.empty();
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }
        };
    }

    @Bean VeilarbaktivitetUnleashClient veilarbaktivitetUnleashClient() {
        return () -> Optional.of(false);
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
