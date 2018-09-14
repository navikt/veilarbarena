package no.nav.fo.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbarena.domain.User;
import org.springframework.kafka.core.KafkaTemplate;

import javax.inject.Inject;

import static no.nav.fo.veilarbarena.config.KafkaConfig.KAFKA_TOPIC;
import static no.nav.json.JsonUtils.toJson;


@Slf4j
public class OppfolgingsbrukerEndringTemplate {
    private KafkaTemplate<String, String> kafkaTemplate;

    @Inject
    public OppfolgingsbrukerEndringTemplate(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    void send(User bruker) {
        final String serialisertBruker = toJson(bruker);

        kafkaTemplate.send(
                KAFKA_TOPIC,
                bruker.getAktoerid().get(),
                serialisertBruker
        );
        log.debug("Bruker: {} har endringer, legger på kafka", bruker.getAktoerid().get());
    }
}
