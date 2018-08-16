package no.nav.fo.veilarbarena.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbarena.domain.Bruker;
import org.springframework.kafka.core.KafkaTemplate;

import javax.inject.Inject;

import static no.nav.fo.veilarbarena.config.KafkaConfig.OPPFOLGINGSBRUKER_MED_ENDRING_SIDEN;


@Slf4j
public class OppfolgingsbrukerEndringTemplate {
    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper brukerMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());


    @Inject
    public OppfolgingsbrukerEndringTemplate(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    void send(Bruker bruker) {
        final String mappedBruker;
        try {
            mappedBruker = brukerMapper.writeValueAsString(bruker);
        } catch (JsonProcessingException e) {
            log.error("Kunne ikke serialisere Bruker", e);
            return;
        }

        kafkaTemplate.send(
                OPPFOLGINGSBRUKER_MED_ENDRING_SIDEN,
                bruker.getAktoerid(),
                mappedBruker
        );
        log.info("Bruker: {} har endringer, legger på kø", bruker.getAktoerid());
    }
}
