package no.nav.veilarbarena.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import no.nav.veilarbarena.config.ApplicationTestConfig;
import no.nav.veilarbarena.domain.api.OppfolgingsbrukerEndretDTO;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.time.ZonedDateTime;

import static no.nav.veilarbarena.config.KafkaTestConfig.consumerFactory;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationTestConfig.class)
@ActiveProfiles("local")
public class KafkaServiceTest {

    @Autowired
    KafkaService kafkaService;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    public void sendBrukerEndret_skal_legge_bruker_pa_topic() {
        DefaultKafkaConsumerFactory<String, String> consumerFactory = consumerFactory(embeddedKafkaBroker.getBrokersAsString());
        Consumer<String, String> consumer = consumerFactory.createConsumer("veilarbarena-test-consumer", "local", "local");

        ZonedDateTime now = ZonedDateTime.now();
        OppfolgingsbrukerEndretDTO brukerEndret = new OppfolgingsbrukerEndretDTO()
                .setFodselsnr("test-fnr")
                .setAktoerid("test-aktorid")
                .setEndret_dato(now);

        kafkaService.sendBrukerEndret(brukerEndret);

        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(5000));
        ConsumerRecord<String, String > record = records.iterator().next();

        JsonObject brukerObj = JsonParser.parseString(record.value()).getAsJsonObject();

        assertEquals("test-aktorid", record.key());
        assertEquals("test-aktorid", brukerObj.get("aktoerid").getAsString());
        assertEquals("test-fnr", brukerObj.get("fodselsnr").getAsString());
        assertEquals(now.toLocalDateTime(), ZonedDateTime.parse(brukerObj.get("endret_dato").getAsString()).toLocalDateTime());
    }

}
