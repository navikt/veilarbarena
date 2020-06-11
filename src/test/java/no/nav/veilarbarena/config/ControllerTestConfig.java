package no.nav.veilarbarena.config;

import no.nav.veilarbarena.controller.InternalController;
import no.nav.veilarbarena.controller.OppfolgingsbrukerController;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        OppfolgingsbrukerController.class,
        InternalController.class,
})
public class ControllerTestConfig {}
