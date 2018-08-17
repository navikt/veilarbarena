package no.nav.fo.veilarbarena.service;

import no.nav.fo.veilarbarena.domain.Bruker;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static no.nav.json.JsonUtils.fromJson;
import static no.nav.json.JsonUtils.toJson;
import static org.assertj.core.api.Assertions.assertThat;

class OppfolgingsbrukerEndringTemplateTest {

    @Test
    void serialiseringOgDeserialiseringAvBruker() {
        final ZonedDateTime tidspunkt = new Timestamp(100000000000L).toLocalDateTime().atZone(ZoneId.systemDefault());
        Bruker opprinneligBruker = new Bruker(
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

        final String serialisertBruker = toJson(opprinneligBruker);
        ConsumerRecord<String, String> cr = new ConsumerRecord<>("testTopic", 1, 1, "testKey", serialisertBruker);

        Bruker deserialisertBruker = fromJson(cr.value(), Bruker.class);

        assertThat(opprinneligBruker.equals(deserialisertBruker)).isTrue();
    }
}