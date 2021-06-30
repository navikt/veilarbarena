package no.nav.veilarbarena.config;

import no.nav.veilarbarena.service.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        AuthService.class,
        KafkaProducerService.class,
        MetricsService.class,
        UnleashService.class,
        ArenaService.class
})
public class ServiceTestConfig {}
