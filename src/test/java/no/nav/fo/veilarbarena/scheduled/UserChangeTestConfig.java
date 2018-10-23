package no.nav.fo.veilarbarena.scheduled;

import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.mockito.Mockito.mock;

@Configuration
@EnableScheduling
public class UserChangeTestConfig {

    @Bean
    public UserChangePublisher cacheInvalidationJob() {
        return new UserChangePublisher(mock(LockingTaskExecutor.class));
    }
}
