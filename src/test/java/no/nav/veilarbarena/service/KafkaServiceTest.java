package no.nav.veilarbarena.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import no.nav.common.json.JsonUtils;
import no.nav.common.kafka.consumer.ConsumeStatus;
import no.nav.common.kafka.consumer.KafkaConsumerClientConfig;
import no.nav.common.kafka.consumer.KafkaConsumerClientImpl;
import no.nav.veilarbarena.config.ApplicationTestConfig;
import no.nav.veilarbarena.controller.response.OppfolgingsbrukerEndretDTO;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.KafkaContainer;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationTestConfig.class)
@ActiveProfiles("local")
public class KafkaServiceTest {

    @Autowired
    KafkaService kafkaService;

    @Autowired
    KafkaContainer kafkaContainer;

    @Test
    public void sendBrukerEndret_skal_legge_bruker_pa_topic() throws JsonProcessingException, InterruptedException {
        ZonedDateTime now = ZonedDateTime.now();
        OppfolgingsbrukerEndretDTO brukerEndret = new OppfolgingsbrukerEndretDTO()
                .setFodselsnr("test-fnr")
                .setAktoerid("test-aktorid")
                .setEndret_dato(now);

        kafkaService.sendBrukerEndret(brukerEndret);

        AtomicReference<ConsumerRecord<String, String>> recordRef = new AtomicReference<>();

        KafkaConsumerClientImpl<String, String> consumerClient = new KafkaConsumerClientImpl<>(new KafkaConsumerClientConfig<>(
                kafkaTestConsumerProperties(kafkaContainer.getBootstrapServers()),
                Map.of("aapen-fo-endringPaaOppfoelgingsBruker-v1-local", (record) -> {
                    recordRef.set(record);
                    return ConsumeStatus.OK;
                })
        ));

        consumerClient.start();

        while (recordRef.get() == null) {
            Thread.sleep(100);
        }

        JsonNode brukerNode = JsonUtils.getMapper().readTree(recordRef.get().value());

        assertEquals("test-aktorid", recordRef.get().key());
        assertEquals("test-aktorid", brukerNode.get("aktoerid").asText());
        assertEquals("test-fnr", brukerNode.get("fodselsnr").asText());
        assertEquals(now.toLocalDateTime(), ZonedDateTime.parse(brukerNode.get("endret_dato").asText()).toLocalDateTime());
    }

    private static Properties kafkaTestConsumerProperties(String brokerUrl) {
        Properties props = new Properties();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 5 * 60 * 1000);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

}
