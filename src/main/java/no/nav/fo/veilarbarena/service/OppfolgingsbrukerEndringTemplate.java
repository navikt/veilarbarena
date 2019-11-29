package no.nav.fo.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbarena.api.OppfolgingsbrukerEndretDTO;
import no.nav.fo.veilarbarena.domain.PersonId;
import no.nav.fo.veilarbarena.domain.PersonId.AktorId;
import no.nav.fo.veilarbarena.domain.User;
import no.nav.metrics.MetricsFactory;
import no.nav.metrics.Timer;
import org.springframework.kafka.core.KafkaTemplate;

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
        log.info("OppfolgingsbrukerEndringTemplate topic: {}", topic);
    }

    void send(User user) {
        final String serialisertBruker = toJson(toDTO(user));
        Timer timer = MetricsFactory.createTimer("bruker.kafka.send").start();
        kafkaTemplate.send(
                topic,
                user.getAktoerid().get(),
                serialisertBruker
        ).addCallback(
                sendResult -> onSuccess(user, timer),
                throwable -> onError(throwable, user, timer)
        );
    }

    private void onSuccess(User user, Timer timer) {
        timer.stop().setSuccess().report();
        leggerBrukerPaKafkaMetrikk(user);
        oppfolgingsbrukerEndringRepository.deleteFeiletBruker(user);
        log.info("Bruker med aktorid {} har lagt på kafka", user.getAktoerid().get());
    }

    private void onError(Throwable throwable, User user, Timer timer) {
        timer.stop().setFailed().report();
        log.error("Kunne ikke publisere melding til kafka-topic", throwable);
        feilVedSendingTilKafkaMetrikk();
        log.info("Forsøker å insertere feilede bruker med aktorid {} i FEILEDE_KAFKA_BRUKERE", user.getAktoerid());
        oppfolgingsbrukerEndringRepository.insertFeiletBruker(user);
    }

    public static OppfolgingsbrukerEndretDTO toDTO(User user) {
        return OppfolgingsbrukerEndretDTO.builder()
                .aktoerid(ofNullable(user.getAktoerid()).map(AktorId::get).orElse(null))
                .fodselsnr(ofNullable(user.getFodselsnr()).map(PersonId::get).orElse(null))
                .formidlingsgruppekode(user.getFormidlingsgruppekode())
                .iserv_fra_dato(user.getIserv_fra_dato())
                .fornavn(user.getFornavn())
                .etternavn(user.getEtternavn())
                .doed_fra_dato(user.getDoed_fra_dato())
                .nav_kontor(user.getNav_kontor())
                .er_doed(user.getEr_doed())
                .fr_kode(user.getFr_kode())
                .har_oppfolgingssak(user.getHar_oppfolgingssak())
                .hovedmaalkode(user.getHovedmaalkode())
                .kvalifiseringsgruppekode(user.getKvalifiseringsgruppekode())
                .rettighetsgruppekode(user.getRettighetsgruppekode())
                .sikkerhetstiltak_type_kode(user.getSikkerhetstiltak_type_kode())
                .sperret_ansatt(user.getSperret_ansatt())
                .endret_dato(user.getEndret_dato())
                .build();
    }
}
