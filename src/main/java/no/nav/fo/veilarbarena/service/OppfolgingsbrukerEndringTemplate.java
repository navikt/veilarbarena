package no.nav.fo.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbarena.api.UserDTO;
import no.nav.fo.veilarbarena.domain.PersonId;
import no.nav.fo.veilarbarena.domain.PersonId.AktorId;
import no.nav.fo.veilarbarena.domain.User;
import org.springframework.kafka.core.KafkaTemplate;

import javax.inject.Inject;

import static java.util.Optional.ofNullable;
import static no.nav.fo.veilarbarena.config.KafkaConfig.KAFKA_TOPIC;
import static no.nav.fo.veilarbarena.utils.FunksjonelleMetrikker.leggerBrukerPaKafkaMetrikk;
import static no.nav.json.JsonUtils.toJson;

@Slf4j
public class OppfolgingsbrukerEndringTemplate {
    private KafkaTemplate<String, String> kafkaTemplate;

    @Inject
    public OppfolgingsbrukerEndringTemplate(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    void send(User bruker) {
        final String serialisertBruker = toJson(toDTO(bruker));

        kafkaTemplate.send(
                KAFKA_TOPIC,
                bruker.getAktoerid().get(),
                serialisertBruker
        );

        leggerBrukerPaKafkaMetrikk(bruker);
        log.debug("Bruker: {} har endringer, legger på kafka", bruker.getAktoerid().get());
    }

    public static UserDTO toDTO(User user) {
        return new UserDTO()
                .setAktoerid(ofNullable(user.getAktoerid()).map(AktorId::get).orElse(null))
                .setFodselsnr(ofNullable(user.getFodselsnr()).map(PersonId::get).orElse(null))
                .setFormidlingsgruppekode(user.getFormidlingsgruppekode())
                .setIserv_fra_dato(user.getIserv_fra_dato());
    }
}
