package no.nav.veilarbarena.config;

import no.nav.common.utils.Credentials;
import no.nav.veilarbarena.kafka.KafkaTopics;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.LoggingProducerListener;

import java.util.HashMap;

import static no.nav.veilarbarena.utils.KafkaUtils.requireKafkaTopicPrefix;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Bean
    public KafkaTopics kafkaTopics() {
        return KafkaTopics.create(requireKafkaTopicPrefix());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(EnvironmentProperties environmentProperties, Credentials serviceUserCredentials) {
        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(kafkaProducerProperties(environmentProperties.getKafkaBrokersUrl(), serviceUserCredentials));
        KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory);
        LoggingProducerListener<String, String> producerListener = new LoggingProducerListener<>();
        producerListener.setIncludeContents(false);
        template.setProducerListener(producerListener);
        return template;
    }

    private static HashMap<String, Object> kafkaBaseProperties(String kafkaBrokersUrl, Credentials serviceUserCredentials) {
        HashMap<String, Object> props = new HashMap<>();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokersUrl);
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + serviceUserCredentials.username + "\" password=\"" + serviceUserCredentials.password + "\";");
        return props;
    }

    private static HashMap<String, Object> kafkaProducerProperties (String kafkaBrokersUrl, Credentials serviceUserCredentials) {
        HashMap<String, Object> props = kafkaBaseProperties(kafkaBrokersUrl, serviceUserCredentials);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "veilarbarena-producer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

}
