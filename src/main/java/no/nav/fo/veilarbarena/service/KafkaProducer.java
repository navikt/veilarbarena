package no.nav.fo.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbarena.domain.Iserv28;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static no.nav.fo.veilarbarena.config.KafkaConfig.ISERV28DAGER_TOPIC;

@Component
@Slf4j
public class KafkaProducer {
    private KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(Iserv28 iserv28) {
        kafkaTemplate.send(
                ISERV28DAGER_TOPIC,
                iserv28.aktorId,
                iserv28.toString()
        );
        log.info("Ident {} lagt på kø", iserv28.aktorId);
    }
}
