package no.nav.veilarbarena.service;

import no.nav.veilarbarena.KafkaTest;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;
import static org.springframework.kafka.test.assertj.KafkaConditions.key;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasValue;

public class KafkaSenderTest extends KafkaTest {

//    @Test
//    public void testSend() throws InterruptedException {
//        String greeting = "Hello Spring Kafka Sender!";
//        sender.send(SENDER_TOPIC, greeting);
//
//        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);
//        assertThat(received, hasValue(greeting));
//        assertThat(received).has(key(null));
//    }
//
//    @Test
//    public void testSend2() throws InterruptedException {
//        String greeting = "Hello Spring Kafka Sender!2222";
//        sender.send(SENDER_TOPIC, greeting);
//
//        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);
//        assertThat(received, hasValue(greeting));
//        assertThat(received).has(key(null));
//    }
}