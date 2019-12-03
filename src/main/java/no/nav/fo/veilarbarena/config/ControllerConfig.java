package no.nav.fo.veilarbarena.config;

import no.nav.fo.veilarbarena.controller.OppfolgingsbrukerController;
import no.nav.fo.veilarbarena.controller.OppfolgingssakController;
import no.nav.fo.veilarbarena.controller.OppfolgingsstatusController;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        OppfolgingsbrukerController.class,
        OppfolgingsstatusController.class,
        OppfolgingssakController.class
})
public class ControllerConfig {
}
