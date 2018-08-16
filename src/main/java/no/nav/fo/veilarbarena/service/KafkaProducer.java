package no.nav.fo.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbarena.domain.Bruker;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static no.nav.fo.veilarbarena.config.KafkaConfig.OPPFOLGINGSBRUKER_MED_ENDRING_SIDEN;

@Component
@Slf4j
public class KafkaProducer {
    private KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(Bruker bruker) {
        kafkaTemplate.send(
                OPPFOLGINGSBRUKER_MED_ENDRING_SIDEN,
                bruker.getAktoerid(),
                bruker.toString()
        );
        log.info("Bruker: {} har endringer, legger på kø", bruker.getAktoerid());
    }
}
