package no.nav.fo.veilarbarena.config;

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
    public static final String OPPFOLGINGSBRUKER_MED_ENDRING_SIDEN = "aapen-fo-endringPaaOppfoelgingsBruker-v1";

    @Bean
    public static Map<String, Object> producerConfigs() {
        HashMap<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "b27apvl00045.preprod.local:8443,b27apvl00046.preprod.local:8443,b27apvl00047.preprod.local:8443");
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
