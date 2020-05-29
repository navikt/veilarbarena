package no.nav.veilarbarena.config;

import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import no.nav.veilarbarena.scheduled.UserChangePublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class UserChangeConfig {

    @Bean
    public UserChangePublisher cacheInvalidationJob(LockingTaskExecutor lockingTaskExecutor) {
        return new UserChangePublisher(lockingTaskExecutor);
    }
}