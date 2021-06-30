package no.nav.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.kafka.producer.feilhandtering.KafkaProducerRecordStorage;
import no.nav.common.kafka.producer.util.ProducerUtils;
import no.nav.veilarbarena.config.KafkaProperties;
import no.nav.veilarbarena.controller.response.OppfolgingsbrukerEndretDTO;
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

    public void publiserEndringPaOppfolgingsbruker(OppfolgingsbrukerEndretDTO bruker) {
        ProducerRecord<String, Object> jsonRecord = new ProducerRecord<>(
                kafkaProperties.getEndringPaaOppfolgingBrukerOnPremTopic(),
                bruker.getAktoerid(),
                bruker
        );

        kafkaProducerRecordStorage.store(ProducerUtils.serializeJsonRecord(jsonRecord));

        metricsService.leggerBrukerPaKafkaMetrikk(bruker);
    }

}
