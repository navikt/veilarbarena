package no.nav.veilarbarena.config;

import no.nav.veilarbarena.controller.InternalController;
import no.nav.veilarbarena.controller.OppfolgingsbrukerController;
import no.nav.veilarbarena.controller.OppfolgingsstatusController;
import no.nav.veilarbarena.controller.YtelserController;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        OppfolgingsbrukerController.class,
        OppfolgingsstatusController.class,
        InternalController.class,
        YtelserController.class
})
public class ControllerTestConfig {}
