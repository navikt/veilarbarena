package no.nav.fo.veilarbarena.service;

import no.nav.fo.veilarbarena.domain.Bruker;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static no.nav.fo.veilarbarena.KafkaTest.SENDER_TOPIC;
import static no.nav.fo.veilarbarena.config.KafkaConfig.KAFKA_TOPIC;
import static no.nav.json.JsonUtils.fromJson;
import static no.nav.json.JsonUtils.toJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.*;

class OppfolgingsbrukerEndringTemplateTest {
    private static final ZonedDateTime TIDSPUNKT = new Timestamp(100000000000L).toLocalDateTime().atZone(ZoneId.systemDefault());
    private static final Bruker BRUKER = new Bruker(
            "test",
            "test",
            "test",
            "test",
            "test",
            TIDSPUNKT,
            "test",
            "test",
            "test",
            "test",
            "test",
            "test",
            "test",
            false,
            TIDSPUNKT,
            TIDSPUNKT
    );

    @Test
    void serialiseringOgDeserialiseringAvBruker() {
        final String serialisertBruker = toJson(BRUKER);
        ConsumerRecord<String, String> cr = new ConsumerRecord<>(SENDER_TOPIC, 1, 1, "testKey", serialisertBruker);

        Bruker deserialisertBruker = fromJson(cr.value(), Bruker.class);

        assertThat(BRUKER.equals(deserialisertBruker)).isTrue();
    }

    @Test
    void leggerBrukerPaTopic() {
        KafkaTemplate<String, String> template = mock(KafkaTemplate.class);
        OppfolgingsbrukerEndringTemplate sender = new OppfolgingsbrukerEndringTemplate(template);
        ArgumentCaptor<String> aktorId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bruker = ArgumentCaptor.forClass(String.class);

        sender.send(BRUKER);

        verify(template, times(1)).send(matches(KAFKA_TOPIC), aktorId.capture(), bruker.capture());
        assertThat(aktorId.getValue()).isEqualTo(BRUKER.getAktoerid());
        assertThat(bruker.getValue()).isEqualTo(toJson(BRUKER));
    }
}