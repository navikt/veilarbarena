package no.nav.veilarbarena.config;

import no.nav.veilarbarena.controller.ArenaControllerTest;
import no.nav.veilarbarena.controller.InternalController;
import no.nav.veilarbarena.controller.OppfolgingsbrukerController;
import no.nav.veilarbarena.controller.OppfolgingsstatusController;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        OppfolgingsbrukerController.class,
        OppfolgingsstatusController.class,
        InternalController.class,
        ArenaControllerTest.class
})
public class ControllerTestConfig {}
