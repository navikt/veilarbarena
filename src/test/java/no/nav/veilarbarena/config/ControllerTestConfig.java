package no.nav.veilarbarena.config;

import no.nav.veilarbarena.controller.*;
import no.nav.veilarbarena.controller.v2.AdminV2Controller;
import no.nav.veilarbarena.controller.v2.ArenaV2Controller;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        OppfolgingsbrukerController.class,
        OppfolgingsstatusController.class,
        InternalController.class,
        ArenaController.class,
        ArenaV2Controller.class,
        AdminController.class,
        AdminV2Controller.class
})
public class ControllerTestConfig {}
