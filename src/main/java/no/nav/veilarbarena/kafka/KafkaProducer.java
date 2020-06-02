package no.nav.veilarbarena.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbarena.domain.User;
import no.nav.veilarbarena.repository.KafkaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static no.nav.common.json.JsonUtils.toJson;
import static org.apache.commons.lang3.ClassUtils.getName;

@Slf4j
@Component
public class KafkaProducer {

    private final KafkaTopics kafkaTopics;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final KafkaRepository kafkaRepository;

    @Autowired
    public KafkaProducer(KafkaTopics kafkaTopics, KafkaTemplate<String, String> kafkaTemplate, KafkaRepository kafkaRepository) {
        this.kafkaTopics = kafkaTopics;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaRepository = kafkaRepository;
    }

    public void sendEndringPaOppfolgingsbruker(User user) {
        send(kafkaTopics.getEndringPaOppfolgingBruker(), user.getAktoerid().get(), toJson(user));
    }

    private void send(String topic, String key, String jsonPayload) {
        kafkaTemplate.send(topic, key, jsonPayload)
                .addCallback(
                        sendResult -> onSuccess(topic, key),
                        throwable -> onError(topic, key, throwable)
                );
    }

    private void onSuccess(String topic, String key) {
        log.info(format("Publiserte melding på topic %s med key %s", topic, key));
    }

    private void onError(String topic, String key, Throwable throwable) {
        log.error(format("Kunne ikke publisere melding på topic %s med key %s \nERROR: %s", topic, key, throwable));
    }

}
