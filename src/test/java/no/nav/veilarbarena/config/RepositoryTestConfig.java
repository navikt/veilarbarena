package no.nav.veilarbarena.config;

import no.nav.veilarbarena.repository.KafkaRepository;
import no.nav.veilarbarena.repository.OppfolgingsbrukerRepository;
import no.nav.veilarbarena.repository.OppfolgingsbrukerSistEndringRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        KafkaRepository.class,
        OppfolgingsbrukerRepository.class,
        OppfolgingsbrukerSistEndringRepository.class,
})
public class RepositoryTestConfig {}
