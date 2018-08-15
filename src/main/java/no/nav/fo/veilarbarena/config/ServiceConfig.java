package no.nav.fo.veilarbarena.config;

import no.nav.fo.veilarbarena.soapproxy.oppfolgingstatus.OppfolgingstatusConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        OppfolgingstatusConfig.class
})
public class ServiceConfig {
}
