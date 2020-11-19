package no.nav.veilarbarena.utils;

import no.nav.common.utils.EnvironmentUtils;

public class KafkaUtils {

    public static String requireKafkaTopicPrefix() {
        return EnvironmentUtils.isDevelopment().orElse(false) ? "q1" : "p";
    }

}
