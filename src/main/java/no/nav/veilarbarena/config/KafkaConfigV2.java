package no.nav.veilarbarena.config;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.kafka.producer.KafkaProducerClient;
import no.nav.common.kafka.producer.feilhandtering.KafkaProducerRecordProcessor;
import no.nav.common.kafka.producer.feilhandtering.KafkaProducerRecordStorage;
import no.nav.common.kafka.producer.feilhandtering.OracleProducerRepository;
import no.nav.common.kafka.producer.util.KafkaProducerClientBuilder;
import no.nav.common.utils.Credentials;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import java.util.List;

import static no.nav.common.kafka.util.KafkaPropertiesPreset.aivenByteProducerProperties;
import static no.nav.common.kafka.util.KafkaPropertiesPreset.onPremByteProducerProperties;

@Configuration
@EnableConfigurationProperties({KafkaProperties.class})
public class KafkaConfigV2 {

    public final static String PRODUCER_CLIENT_ID = "veilarbarena-producer";

    private final KafkaProducerRecordProcessor onPremProducerRecordProcessor;

    private final KafkaProducerRecordProcessor aivenProducerRecordProcessor;

    private final KafkaProducerRecordStorage producerRecordStorage;


    public KafkaConfigV2(
            JdbcTemplate jdbcTemplate,
            LeaderElectionClient leaderElectionClient,
            KafkaProperties kafkaProperties,
            Credentials credentials,
            MeterRegistry meterRegistry
    ) {
        OracleProducerRepository oracleProducerRepository = new OracleProducerRepository(jdbcTemplate.getDataSource());

        KafkaProducerClient<byte[], byte[]> onPremProducerClient = KafkaProducerClientBuilder.<byte[], byte[]>builder()
                .withProperties(onPremByteProducerProperties(PRODUCER_CLIENT_ID, kafkaProperties.brokersUrl, credentials))
                .withMetrics(meterRegistry)
                .build();

        KafkaProducerClient<byte[], byte[]> aivenProducerClient = KafkaProducerClientBuilder.<byte[], byte[]>builder()
                .withProperties(aivenByteProducerProperties(PRODUCER_CLIENT_ID))
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
