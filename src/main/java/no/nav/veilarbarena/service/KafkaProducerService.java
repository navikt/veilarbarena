package no.nav.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.kafka.producer.feilhandtering.KafkaProducerRecordStorage;
import no.nav.common.kafka.producer.util.ProducerUtils;
import no.nav.pto_schema.kafka.json.topic.onprem.EndringPaaOppfoelgingsBrukerV1;
import no.nav.veilarbarena.config.KafkaProperties;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaProducerService {

    private final KafkaProducerRecordStorage kafkaProducerRecordStorage;

    private final KafkaProperties kafkaProperties;

    private final MetricsService metricsService;

    public KafkaProducerService(
            KafkaProducerRecordStorage kafkaProducerRecordStorage,
            KafkaProperties kafkaProperties,
            MetricsService metricsService
    ) {
        this.kafkaProducerRecordStorage = kafkaProducerRecordStorage;
        this.kafkaProperties = kafkaProperties;
        this.metricsService = metricsService;
    }

    public void publiserEndringPaOppfolgingsbrukerOnPrem(EndringPaaOppfoelgingsBrukerV1 bruker) {
        ProducerRecord<String, Object> jsonRecord = new ProducerRecord<>(
                kafkaProperties.getEndringPaaOppfolgingBrukerOnPremTopic(),
                bruker.getAktoerid(),
                bruker
        );

        kafkaProducerRecordStorage.store(ProducerUtils.serializeJsonRecord(jsonRecord));

        metricsService.leggerBrukerPaKafkaMetrikk(bruker);
    }

    public void publiserEndringPaOppfolgingsbrukerAiven(EndringPaaOppfoelgingsBrukerV1 bruker) {
        ProducerRecord<String, Object> jsonRecord = new ProducerRecord<>(
                kafkaProperties.getEndringPaaOppfolgingBrukerAivenTopic(),
                bruker.getAktoerid(),
                bruker
        );

        kafkaProducerRecordStorage.store(ProducerUtils.serializeJsonRecord(jsonRecord));
    }

}
