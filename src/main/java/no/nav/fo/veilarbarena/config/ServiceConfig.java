package no.nav.fo.veilarbarena.config;

import no.nav.apiapp.security.veilarbabac.VeilarbAbacPepClient;
import no.nav.dialogarena.aktor.AktorConfig;
import no.nav.dialogarena.aktor.AktorService;
import no.nav.fo.veilarbarena.service.AuthService;
import no.nav.fo.veilarbarena.service.BrukereMedOppdateringService;
import no.nav.fo.veilarbarena.service.OppfolgingsbrukerEndringTemplate;
import no.nav.fo.veilarbarena.soapproxy.oppfolgingstatus.OppfolgingstatusConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        OppfolgingstatusConfig.class,
        KafkaConfig.class,
        AktorConfig.class
})
public class ServiceConfig {

    @Bean
    public BrukereMedOppdateringService brukereMedOppdateringService(OppfolgingsbrukerEndringTemplate oppfolgingsbrukerEndringTemplate, AktorService aktorService) {
        return new BrukereMedOppdateringService(oppfolgingsbrukerEndringTemplate, aktorService);
    }

    @Bean
    public AuthService authService(AktorService aktorService, VeilarbAbacPepClient pepClient) {
        return new AuthService(aktorService, pepClient);
    }
}
