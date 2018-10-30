package no.nav.fo.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbarena.api.UserDTO;
import no.nav.fo.veilarbarena.domain.PersonId;
import no.nav.fo.veilarbarena.domain.PersonId.AktorId;
import no.nav.fo.veilarbarena.domain.User;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static java.util.Optional.ofNullable;
import static no.nav.fo.veilarbarena.utils.FunksjonelleMetrikker.feilVedSendingTilKafkaMetrikk;
import static no.nav.fo.veilarbarena.utils.FunksjonelleMetrikker.leggerBrukerPaKafkaMetrikk;
import static no.nav.json.JsonUtils.toJson;

@Slf4j
public class OppfolgingsbrukerEndringTemplate {
    private final String topic;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OppfolgingsbrukerEndringRepository oppfolgingsbrukerEndringRepository;

    public OppfolgingsbrukerEndringTemplate(KafkaTemplate<String, String> kafkaTemplate, OppfolgingsbrukerEndringRepository oppfolgingsbrukerEndringRepository, String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.oppfolgingsbrukerEndringRepository = oppfolgingsbrukerEndringRepository;
        this.topic = topic;
    }

    void send(User user) {
        final String serialisertBruker = toJson(toDTO(user));

        kafkaTemplate.send(
                topic,
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

    public static UserDTO toDTO(User user) {
        return new UserDTO()
                .setAktoerid(ofNullable(user.getAktoerid()).map(AktorId::get).orElse(null))
                .setFodselsnr(ofNullable(user.getFodselsnr()).map(PersonId::get).orElse(null))
                .setFormidlingsgruppekode(user.getFormidlingsgruppekode())
                .setIserv_fra_dato(user.getIserv_fra_dato())
                .setFornavn(user.getFornavn())
                .setEtternavn(user.getEtternavn())
                .setDoed_fra_dato(user.getDoed_fra_dato())
                .setNav_kontor(user.getNav_kontor())
                .setEr_doed(user.getEr_doed())
                .setFr_kode(user.getFr_kode())
                .setHar_oppfolgingssak(user.getHar_oppfolgingssak())
                .setHovedmaalkode(user.getHovedmaalkode())
                .setKvalifiseringsgruppekode(user.getKvalifiseringsgruppekode())
                .setRettighetsgruppekode(user.getRettighetsgruppekode())
                .setSikkerhetstiltak_type_kode(user.getSikkerhetstiltak_type_kode())
                .setSperret_ansatt(user.getSperret_ansatt())
                .setEndret_dato(user.getEndret_dato());
    }
}
