package no.nav.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.kafka.producer.feilhandtering.KafkaProducerRecordStorage;
import no.nav.common.kafka.producer.util.ProducerUtils;
import no.nav.pto_schema.kafka.json.topic.onprem.EndringPaaOppfoelgingsBrukerV1;
import no.nav.pto_schema.kafka.json.topic.onprem.EndringPaaOppfoelgingsBrukerV2;
import no.nav.veilarbarena.config.KafkaProperties;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaProducerService {

    private final KafkaProducerRecordStorage kafkaProducerRecordStorage;

    private final KafkaProperties kafkaProperties;

    public KafkaProducerService(
            KafkaProducerRecordStorage kafkaProducerRecordStorage,
            KafkaProperties kafkaProperties
    ) {
        this.kafkaProducerRecordStorage = kafkaProducerRecordStorage;
        this.kafkaProperties = kafkaProperties;
    }

    public void publiserEndringPaOppfolgingsbrukerV1OnPrem(EndringPaaOppfoelgingsBrukerV1 bruker) {
        ProducerRecord<String, Object> jsonRecord = new ProducerRecord<>(
                kafkaProperties.getEndringPaaOppfolgingBrukerOnPremTopic(),
                bruker.getAktoerid(),
                bruker
        );

        kafkaProducerRecordStorage.store(ProducerUtils.serializeJsonRecord(jsonRecord));
    }

    public void publiserEndringPaOppfolgingsbrukerV2Aiven(EndringPaaOppfoelgingsBrukerV2 bruker) {
        ProducerRecord<String, Object> jsonRecord = new ProducerRecord<>(
                kafkaProperties.getEndringPaaOppfolgingBrukerAivenTopic(),
                bruker.getFodselsnummer(),
                bruker
        );

        kafkaProducerRecordStorage.store(ProducerUtils.serializeJsonRecord(jsonRecord));
    }

}
