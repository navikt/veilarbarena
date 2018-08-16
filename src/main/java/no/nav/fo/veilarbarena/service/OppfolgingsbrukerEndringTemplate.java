package no.nav.fo.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbarena.domain.Bruker;
import org.springframework.kafka.core.KafkaTemplate;

import javax.inject.Inject;

import static no.nav.fo.veilarbarena.config.KafkaConfig.OPPFOLGINGSBRUKER_MED_ENDRING_SIDEN;


@Slf4j
public class OppfolgingsbrukerEndringTemplate {
    private KafkaTemplate<String, Bruker> kafkaTemplate;

    @Inject
    public OppfolgingsbrukerEndringTemplate(KafkaTemplate<String, Bruker> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    void send(Bruker bruker) {
        kafkaTemplate.send(
                OPPFOLGINGSBRUKER_MED_ENDRING_SIDEN,
                bruker.getAktoerid(),
                bruker
        );
        log.info("Bruker: {} har endringer, legger på kø", bruker.getAktoerid());
    }
}
