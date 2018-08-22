package no.nav.fo.veilarbarena.config;

import no.nav.dialogarena.config.fasit.FasitUtils;
import no.nav.fo.veilarbarena.service.OppfolgingsbrukerEndringTemplate;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    public static final String KAFKA_TOPIC = FasitUtils.getApplicationProperties("veilarbarena.kafka.properties").getProperty("topic");
    private static final String KAFKA_BROKERS = FasitUtils.getBaseUrl("kafka-brokers", FasitUtils.Zone.FSS);

    @Bean
    public static Map<String, Object> producerConfigs() {
        HashMap<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKERS);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "veilarbarena-producer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return props;
    }

    @Bean
    public static ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public OppfolgingsbrukerEndringTemplate oppfolgingsbrukerEndringTemplate() {
        return new OppfolgingsbrukerEndringTemplate(kafkaTemplate());
    }
}
