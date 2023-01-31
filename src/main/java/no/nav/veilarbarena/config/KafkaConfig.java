package no.nav.veilarbarena.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.kafka.producer.KafkaProducerClient;
import no.nav.common.kafka.producer.feilhandtering.KafkaProducerRecordProcessor;
import no.nav.common.kafka.producer.feilhandtering.KafkaProducerRecordStorage;
import no.nav.common.kafka.producer.util.KafkaProducerClientBuilder;
import no.nav.common.kafka.spring.OracleJdbcTemplateProducerRepository;
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
        Properties ProducerClientProperties;
    }

    public final static String PRODUCER_CLIENT_ID = "veilarbarena-producer";

    private final KafkaProducerRecordProcessor ProducerRecordProcessor;

    private final KafkaProducerRecordStorage producerRecordStorage;

    public KafkaConfig(
            JdbcTemplate jdbcTemplate,
            LeaderElectionClient leaderElectionClient,
            EnvironmentContext environmentContext,
            KafkaProperties kafkaProperties,
            MeterRegistry meterRegistry
    ) {
        OracleJdbcTemplateProducerRepository oracleProducerRepository = new OracleJdbcTemplateProducerRepository(jdbcTemplate);

        KafkaProducerClient<byte[], byte[]> ProducerClient = KafkaProducerClientBuilder.<byte[], byte[]>builder()
                .withProperties(environmentContext.ProducerClientProperties)
                .withMetrics(meterRegistry)
                .build();

        ProducerRecordProcessor = new KafkaProducerRecordProcessor(
                oracleProducerRepository,
                ProducerClient,
                leaderElectionClient,
                List.of(
                        kafkaProperties.endringPaaOppfolgingsbrukerTopic
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
        ProducerRecordProcessor.start();
    }

}
