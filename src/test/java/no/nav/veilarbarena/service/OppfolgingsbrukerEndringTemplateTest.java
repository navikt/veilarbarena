package no.nav.veilarbarena.service;

import no.nav.veilarbarena.domain.api.OppfolgingsbrukerEndretDTO;
import no.nav.veilarbarena.domain.User;
import no.nav.veilarbarena.KafkaTest;
import no.nav.veilarbarena.kafka.OppfolgingsbrukerEndringTemplate;
import no.nav.veilarbarena.repository.KafkaRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.ListenableFuture;

import static no.nav.veilarbarena.Utils.lagNyBruker;
import static no.nav.json.JsonUtils.fromJson;
import static no.nav.json.JsonUtils.toJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.*;

class OppfolgingsbrukerEndringTemplateTest {

    @Test
    void serialiseringOgDeserialiseringAvBruker() {
        final User user = lagNyBruker();
        final String serialisertBruker = toJson(OppfolgingsbrukerEndringTemplate.toDTO(user));
        ConsumerRecord<String, String> cr = new ConsumerRecord<>(KafkaTest.SENDER_TOPIC, 1, 1, "testKey", serialisertBruker);

        OppfolgingsbrukerEndretDTO deserialisertBruker = fromJson(cr.value(), OppfolgingsbrukerEndretDTO.class);

        assertThat(user.getIserv_fra_dato()).isEqualTo(deserialisertBruker.getIserv_fra_dato());
        assertThat(user.getAktoerid().get()).isEqualTo(deserialisertBruker.getAktoerid());
        assertThat(user.getFodselsnr().get()).isEqualTo(deserialisertBruker.getFodselsnr());
        assertThat(user.getDoed_fra_dato()).isEqualTo(deserialisertBruker.getDoed_fra_dato());
        assertThat(user.getEndret_dato()).isEqualTo(deserialisertBruker.getEndret_dato());
    }

    @Test
    void leggerBrukerPaTopic() {
        final User user = lagNyBruker();

        System.setProperty("ENDRING_BRUKER_TOPIC", "topic");
        System.setProperty("KAFKA_BROKERS_URL", "testing.localhost,13337.localhost");
        System.setProperty("SRVVEILARBARENA_USERNAME", "srvveilarbarena");
        System.setProperty("SRVVEILARBARENA_PASSWORD", "test123");
        String testTopic = "test-topic";

        KafkaTemplate<String, String> template = mock(KafkaTemplate.class);
        when(template.send(any(), any(), any())).thenReturn(mock(ListenableFuture.class));
        OppfolgingsbrukerEndringTemplate sender = new OppfolgingsbrukerEndringTemplate(template, mock(KafkaRepository.class), testTopic);
        ArgumentCaptor<String> aktorId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bruker = ArgumentCaptor.forClass(String.class);

        sender.send(user);

        verify(template, times(1)).send(matches(testTopic), aktorId.capture(), bruker.capture());
        assertThat(aktorId.getValue()).isEqualTo(user.getAktoerid().get());
        assertThat(bruker.getValue()).isEqualTo(toJson(OppfolgingsbrukerEndringTemplate.toDTO(user)));
    }
}
