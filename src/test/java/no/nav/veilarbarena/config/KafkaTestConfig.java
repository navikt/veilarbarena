package no.nav.veilarbarena.config;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.kafka.producer.KafkaProducerClient;
import no.nav.common.kafka.producer.feilhandtering.KafkaProducerRecordProcessor;
import no.nav.common.kafka.producer.feilhandtering.KafkaProducerRecordStorage;
import no.nav.common.kafka.producer.feilhandtering.OracleProducerRepository;
import no.nav.common.kafka.producer.util.KafkaProducerClientBuilder;
import no.nav.common.kafka.util.KafkaPropertiesBuilder;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Properties;

import static no.nav.veilarbarena.config.KafkaConfig.PRODUCER_CLIENT_ID;

@Configuration
@EnableConfigurationProperties({KafkaProperties.class})
public class KafkaTestConfig {

    public static final String KAFKA_IMAGE = "confluentinc/cp-kafka:5.4.3";

    private final KafkaProducerRecordProcessor onPremProducerRecordProcessor;

    private final KafkaProducerRecordProcessor aivenProducerRecordProcessor;

    private final KafkaProducerRecordStorage producerRecordStorage;

    public KafkaTestConfig(
            JdbcTemplate jdbcTemplate,
            LeaderElectionClient leaderElectionClient,
            KafkaProperties kafkaProperties,
            MeterRegistry meterRegistry
    ) {
        KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE));
        kafkaContainer.start();

        OracleProducerRepository oracleProducerRepository = new OracleProducerRepository(jdbcTemplate.getDataSource());

        Properties properties = KafkaPropertiesBuilder.producerBuilder()
                .withBrokerUrl(kafkaContainer.getBootstrapServers())
                .withProducerId(PRODUCER_CLIENT_ID)
                .withSerializers(ByteArraySerializer.class, ByteArraySerializer.class)
                .build();

        KafkaProducerClient<byte[], byte[]> onPremProducerClient = KafkaProducerClientBuilder.<byte[], byte[]>builder()
                .withProperties(properties)
                .withMetrics(meterRegistry)
                .build();

        KafkaProducerClient<byte[], byte[]> aivenProducerClient = KafkaProducerClientBuilder.<byte[], byte[]>builder()
                .withProperties(properties)
                .withMetrics(meterRegistry)
                .build();

        onPremProducerRecordProcessor = new KafkaProducerRecordProcessor(
                oracleProducerRepository,
                onPremProducerClient,
                leaderElectionClient,
                List.of(
                        kafkaProperties.endringPaaOppfolgingBrukerOnPremTopic
                )
        );

        aivenProducerRecordProcessor = new KafkaProducerRecordProcessor(
                oracleProducerRepository,
                aivenProducerClient,
                leaderElectionClient,
                List.of(
                        kafkaProperties.endringPaaOppfolgingBrukerAivenTopic
                )
        );

        producerRecordStorage = new KafkaProducerRecordStorage(oracleProducerRepository);
    }

    @Bean
    public KafkaProducerRecordStorage kafkaProducerRecordStorage() {
        return producerRecordStorage;
    }

    @PostConstruct
    public void start() {
        onPremProducerRecordProcessor.start();
        aivenProducerRecordProcessor.start();
    }

}
