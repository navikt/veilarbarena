package no.nav.veilarbarena.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.kafka.producer.KafkaProducerClient;
import no.nav.common.kafka.producer.feilhandtering.KafkaProducerRecordProcessor;
import no.nav.common.kafka.producer.feilhandtering.KafkaProducerRecordStorage;
import no.nav.common.kafka.producer.feilhandtering.OracleProducerRepository;
import no.nav.common.kafka.producer.util.KafkaProducerClientBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties({KafkaProperties.class})
public class KafkaConfig {

    @Data
    @Accessors(chain = true)
    public static class EnvironmentContext {
        Properties onPremProducerClientProperties;
        Properties aivenProducerClientProperties;
    }

    public final static String PRODUCER_CLIENT_ID = "veilarbarena-producer";

    private final KafkaProducerRecordProcessor onPremProducerRecordProcessor;

    private final KafkaProducerRecordProcessor aivenProducerRecordProcessor;

    private final KafkaProducerRecordStorage producerRecordStorage;

    public KafkaConfig(
            JdbcTemplate jdbcTemplate,
            LeaderElectionClient leaderElectionClient,
            EnvironmentContext environmentContext,
            KafkaProperties kafkaProperties,
            MeterRegistry meterRegistry
    ) {
        OracleProducerRepository oracleProducerRepository = new OracleProducerRepository(jdbcTemplate.getDataSource());

        KafkaProducerClient<byte[], byte[]> onPremProducerClient = KafkaProducerClientBuilder.<byte[], byte[]>builder()
                .withProperties(environmentContext.onPremProducerClientProperties)
                .withMetrics(meterRegistry)
                .build();

        KafkaProducerClient<byte[], byte[]> aivenProducerClient = KafkaProducerClientBuilder.<byte[], byte[]>builder()
                .withProperties(environmentContext.aivenProducerClientProperties)
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
