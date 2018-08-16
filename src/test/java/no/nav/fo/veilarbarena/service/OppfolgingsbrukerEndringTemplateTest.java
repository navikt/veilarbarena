package no.nav.fo.veilarbarena.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.fo.veilarbarena.domain.Bruker;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class OppfolgingsbrukerEndringTemplateTest {

    @Test
    void serialiseringOgDeserialiseringAvBruker() throws IOException {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        final ZonedDateTime tidspunkt = new Timestamp(100000000000L).toLocalDateTime().atZone(ZoneId.systemDefault());

        Bruker b = new Bruker(
                1L,
                "test",
                "test",
                "test",
                "test",
                "test",
                tidspunkt,
                "test",
                "test",
                "test",
                "test",
                "test",
                "test",
                "test",
                false,
                tidspunkt,
                tidspunkt
        );

        final String serialisertBruker = mapper.writeValueAsString(b);
        ConsumerRecord<String, String> cr = new ConsumerRecord<>("testTopic", 1, 1, "testKey", serialisertBruker);

        Bruker deserialisertBruker = mapper.readValue(cr.value(), Bruker.class);
        assertThat(Objects.equals(deserialisertBruker, serialisertBruker));
    }

}