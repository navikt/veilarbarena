package no.nav.fo.veilarbarena.service;

import no.nav.fo.veilarbarena.api.UserDTO;
import no.nav.fo.veilarbarena.domain.PersonId;
import no.nav.fo.veilarbarena.domain.User;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static no.nav.fo.veilarbarena.KafkaTest.SENDER_TOPIC;
import static no.nav.fo.veilarbarena.service.OppfolgingsbrukerEndringTemplate.toDTO;
import static no.nav.json.JsonUtils.fromJson;
import static no.nav.json.JsonUtils.toJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.*;

class OppfolgingsbrukerEndringTemplateTest {
    private static final ZonedDateTime TIDSPUNKT = new Timestamp(100000000000L).toLocalDateTime().atZone(ZoneId.systemDefault());
    private static final User BRUKER = new User(
            PersonId.aktorId("test"),
            PersonId.fnr("test"),
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
            false,
            false,
            false,
            TIDSPUNKT,
            TIDSPUNKT
    );

    @Test
    void serialiseringOgDeserialiseringAvBruker() {
        final String serialisertBruker = toJson(toDTO(BRUKER));
        ConsumerRecord<String, String> cr = new ConsumerRecord<>(SENDER_TOPIC, 1, 1, "testKey", serialisertBruker);

        UserDTO deserialisertBruker = fromJson(cr.value(), UserDTO.class);
        assertThat(BRUKER.getIserv_fra_dato()).isEqualTo(deserialisertBruker.getIserv_fra_dato());
        assertThat(BRUKER.getAktoerid().get()).isEqualTo(deserialisertBruker.getAktoerid());
        assertThat(BRUKER.getFodselsnr().get()).isEqualTo(deserialisertBruker.getFodselsnr());
        assertThat(BRUKER.getDoed_fra_dato()).isEqualTo(deserialisertBruker.getDoed_fra_dato());
        assertThat(BRUKER.getEndret_dato()).isEqualTo(deserialisertBruker.getEndret_dato());
    }

    @Test
    void leggerBrukerPaTopic() {
        System.setProperty("ENDRING_BRUKER_TOPIC", "topic");
        System.setProperty("KAFKA_BROKERS_URL", "testing.localhost,13337.localhost");
        System.setProperty("SRVVEILARBARENA_USERNAME", "srvveilarbarena");
        System.setProperty("SRVVEILARBARENA_PASSWORD", "test123");
        String testTopic = "test-topic";

        KafkaTemplate<String, String> template = mock(KafkaTemplate.class);
        OppfolgingsbrukerEndringTemplate sender = new OppfolgingsbrukerEndringTemplate(template, testTopic);
        ArgumentCaptor<String> aktorId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bruker = ArgumentCaptor.forClass(String.class);

        sender.send(BRUKER);

        verify(template, times(1)).send(matches(testTopic), aktorId.capture(), bruker.capture());
        assertThat(aktorId.getValue()).isEqualTo(BRUKER.getAktoerid().get());
        assertThat(bruker.getValue()).isEqualTo(toJson(toDTO(BRUKER)));
    }
}
