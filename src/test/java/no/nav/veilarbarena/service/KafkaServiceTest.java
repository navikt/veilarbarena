package no.nav.veilarbarena.service;

import no.nav.veilarbarena.config.ApplicationTestConfig;
import no.nav.veilarbarena.domain.api.OppfolgingsbrukerEndretDTO;
import no.nav.veilarbarena.kafka.KafkaTopics;
import org.apache.kafka.clients.consumer.*;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import static no.nav.veilarbarena.utils.TestUtils.verifiserAsynkront;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationTestConfig.class)
@ActiveProfiles("local")
public class KafkaServiceTest {

    @Autowired
    KafkaService kafkaService;

    @Autowired
    KafkaTopics kafkaTopics;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    public void sendBrukerEndret_skal_legge_bruker_pa_topic() {
        OppfolgingsbrukerEndretDTO brukerEndret = new OppfolgingsbrukerEndretDTO()
                .setFodselsnr("test-fnr")
                .setAktoerid("test-aktorid")
                .setEndret_dato(ZonedDateTime.now());

        kafkaService.sendBrukerEndret(brukerEndret);

        // TODO: Verifiser at bruker er sendt

//        MockConsumer<String, String> mockConsumer = new MockConsumer<>( OffsetResetStrategy.EARLIEST );
////        KafkaConsumer<String, String> new KafkaConsumer<String, String>().commitSync();
//        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(mockConsumer);
//        ConsumerRecords<String, String> records = mockConsumer.poll(Duration.ofMillis(100));
//
//        verifiserAsynkront(10, TimeUnit.SECONDS, () -> {
//            records.iterator().forEachRemaining((s1) -> {
//                System.out.println(s1);
//            });
//        });
    }


}
