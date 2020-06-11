package no.nav.veilarbarena.kafka;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KafkaTopics {

    private String endringPaOppfolgingBruker;

    public static KafkaTopics create(String topicPrefix) {
        KafkaTopics kafkaTopics = new KafkaTopics();
        kafkaTopics.setEndringPaOppfolgingBruker("aapen-fo-endringPaaOppfoelgingsBruker-v1-" + topicPrefix);
        return kafkaTopics;
    }

    public String[] getAllTopics() {
        return new String[]{this.getEndringPaOppfolgingBruker()};
    }

}
