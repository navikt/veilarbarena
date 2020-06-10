package no.nav.veilarbarena.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import no.nav.common.json.JsonUtils;
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
    public void sendBrukerEndret_skal_legge_bruker_pa_topic() throws JsonProcessingException {
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

        JsonNode brukerNode = JsonUtils.getMapper().readTree(record.value());

        assertEquals("test-aktorid", record.key());
        assertEquals("test-aktorid", brukerNode.get("aktoerid").asText());
        assertEquals("test-fnr", brukerNode.get("fodselsnr").asText());
        assertEquals(now.toLocalDateTime(), ZonedDateTime.parse(brukerNode.get("endret_dato").asText()).toLocalDateTime());
    }

}
