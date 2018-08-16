package no.nav.fo.veilarbarena.config;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;
import java.util.Properties;

@Configuration
public class KafkaConfig {
    public static final String OPPFOLGINGSBRUKER_MED_ENDRING_SIDEN = "aapen-fo-endringPaaOppfoelgingsBruker-v1";

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>((Map<String, Object>) createProducer());
    }

    public static Producer<String, String> createProducer() {
        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "b27apvl00045.preprod.local:8443,b27apvl00046.preprod.local:8443,b27apvl00047.preprod.local:8443");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "veilarbarena-producer");

        return new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());
    }
}
