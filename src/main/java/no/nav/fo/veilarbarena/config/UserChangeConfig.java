package no.nav.fo.veilarbarena.config;

import no.nav.fo.veilarbarena.scheduled.UserChangePublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class UserChangeConfig {
    @Bean
    public UserChangePublisher cacheInvalidationJob() {
        return new UserChangePublisher();
    }
}
