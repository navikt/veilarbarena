package no.nav.fo.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbarena.domain.User;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static no.nav.fo.veilarbarena.config.KafkaConfig.KAFKA_TOPIC;
import static no.nav.fo.veilarbarena.utils.FunksjonelleMetrikker.feilVedSendingTilKafkaMetrikk;
import static no.nav.fo.veilarbarena.utils.FunksjonelleMetrikker.leggerBrukerPaKafkaMetrikk;
import static no.nav.json.JsonUtils.toJson;

@Component
@Slf4j
public class OppfolgingsbrukerEndringTemplate {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OppfolgingsbrukerEndringRepository oppfolgingsbrukerEndringRepository;

    @Inject
    public OppfolgingsbrukerEndringTemplate(KafkaTemplate<String, String> kafkaTemplate, OppfolgingsbrukerEndringRepository oppfolgingsbrukerEndringRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.oppfolgingsbrukerEndringRepository = oppfolgingsbrukerEndringRepository;
    }

    void send(User user) {
        final String serialisertBruker = toJson(user);

        kafkaTemplate.send(
                KAFKA_TOPIC,
                user.getAktoerid().get(),
                serialisertBruker
        ).addCallback(
                sendResult -> onSuccess(user),
                throwable -> onError(throwable, user)
        );

        log.debug("Bruker: {} har endringer, legger på kafka", user.getAktoerid().get());
    }

    private void onSuccess(User user) {
        leggerBrukerPaKafkaMetrikk(user);
        oppfolgingsbrukerEndringRepository.deleteFeiletBruker(user);
    }

    private void onError(Throwable throwable, User user) {
        log.error("Kunne ikke publisere melding til kafka-topic", throwable);
        feilVedSendingTilKafkaMetrikk();
        oppfolgingsbrukerEndringRepository.insertFeiletBruker(user);
    }
}
