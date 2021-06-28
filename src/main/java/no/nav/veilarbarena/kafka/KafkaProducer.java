package no.nav.veilarbarena.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbarena.controller.response.OppfolgingsbrukerEndretDTO;
import no.nav.veilarbarena.repository.KafkaRepository;
import no.nav.veilarbarena.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static no.nav.common.json.JsonUtils.toJson;

@Slf4j
@Component
public class KafkaProducer {

    private final KafkaTopics kafkaTopics;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final KafkaRepository kafkaRepository;

    private final MetricsService metricsService;

    @Autowired
    public KafkaProducer(
            KafkaTopics kafkaTopics,
            KafkaTemplate<String, String> kafkaTemplate,
            KafkaRepository kafkaRepository,
            MetricsService metricsService
    ) {
        this.kafkaTopics = kafkaTopics;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaRepository = kafkaRepository;
        this.metricsService = metricsService;
    }

    public void sendEndringPaOppfolgingsbruker(OppfolgingsbrukerEndretDTO bruker, boolean harFeiletTidligere) {
        String brukerFnr = bruker.getFodselsnr();
        String topic = kafkaTopics.getEndringPaOppfolgingBruker();
        String key = bruker.getAktoerid();
        String payload = toJson(bruker);

        if (!harFeiletTidligere) {
            metricsService.leggerBrukerPaKafkaMetrikk(bruker);
        }

        kafkaTemplate.send(topic, key, payload)
                .addCallback(
                        sendResult -> onSuccess(brukerFnr, harFeiletTidligere, topic, key),
                        throwable -> onError(brukerFnr, harFeiletTidligere, topic, key, throwable)
                );
    }

    private void onSuccess(String brukerFnr, boolean harFeiletTidligere, String topic, String key) {
        log.info(format("Publiserte melding på topic %s med key %s", topic, key));

        if (harFeiletTidligere) {
            log.info(format("Sletter tidligere feilet bruker fra topic %s med key %s", topic, key));
            kafkaRepository.deleteFeiletBruker(brukerFnr);
        }
    }

    private void onError(String brukerFnr, boolean harFeiletTidligere, String topic, String key, Throwable throwable) {
        log.error(format("Kunne ikke publisere melding på topic %s med key %s \nERROR: %s", topic, key, throwable));

        metricsService.feilVedSendingTilKafkaMetrikk();

        if (!harFeiletTidligere) {
            log.info(format("Lagrer feilet melding for topic %s med key %s", topic, key));
            kafkaRepository.insertFeiletBruker(brukerFnr);
        }
    }

}
