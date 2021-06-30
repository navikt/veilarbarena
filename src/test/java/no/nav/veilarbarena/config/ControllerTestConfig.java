package no.nav.veilarbarena.config;

import no.nav.veilarbarena.controller.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        OppfolgingsbrukerController.class,
        OppfolgingsstatusController.class,
        InternalController.class,
        ArenaController.class,
        AdminController.class
})
public class ControllerTestConfig {}
