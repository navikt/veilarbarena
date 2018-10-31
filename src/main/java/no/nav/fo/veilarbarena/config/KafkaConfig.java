package no.nav.fo.veilarbarena.config;

import no.nav.fo.veilarbarena.selftest.KafkaHelsesjekk;
import no.nav.fo.veilarbarena.service.OppfolgingsbrukerEndringRepository;
import no.nav.fo.veilarbarena.service.OppfolgingsbrukerEndringTemplate;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.*;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;
import static no.nav.sbl.util.EnvironmentUtils.requireEnvironmentName;

@Configuration
@Import({OppfolgingsbrukerEndringTemplate.class, KafkaHelsesjekk.class})
public class KafkaConfig {

    private static final String KAFKA_BROKERS = getRequiredProperty("KAFKA_BROKERS_URL");
    private static final String USERNAME = getRequiredProperty("SRVVEILARBARENA_USERNAME");
    private static final String PASSWORD = getRequiredProperty("SRVVEILARBARENA_PASSWORD");

    @Bean
    public static Map<String, Object> producerConfigs() {
        HashMap<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKERS);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "veilarbarena-producer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + USERNAME + "\" password=\"" + PASSWORD + "\";");

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
    public OppfolgingsbrukerEndringRepository oppfolgingsbrukerEndringRepository() {
        return new OppfolgingsbrukerEndringRepository();
    }

    @Bean
    public OppfolgingsbrukerEndringTemplate oppfolgingsbrukerEndringTemplate() {
        return new OppfolgingsbrukerEndringTemplate(kafkaTemplate(), oppfolgingsbrukerEndringRepository(), "aapen-fo-endringPaaOppfoelgingsBruker-v1-" + requireEnvironmentName());
    }

}
