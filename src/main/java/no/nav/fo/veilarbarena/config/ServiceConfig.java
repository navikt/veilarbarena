package no.nav.fo.veilarbarena.config;

import no.nav.fo.veilarbarena.service.AktoerRegisterService;
import no.nav.apiapp.security.veilarbabac.VeilarbAbacPepClient;
import no.nav.fo.veilarbarena.service.AuthService;
import no.nav.fo.veilarbarena.service.BrukereMedOppdateringService;
import no.nav.fo.veilarbarena.service.OppfolgingsbrukerEndringTemplate;
import no.nav.dialogarena.aktor.AktorConfig;
import no.nav.fo.veilarbarena.service.*;
import no.nav.fo.veilarbarena.soapproxy.oppfolgingstatus.OppfolgingstatusConfig;
import no.nav.fo.veilarbarena.utils.ArenaOrdsTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        OppfolgingstatusConfig.class,
        KafkaConfig.class,
        AktoerRegisterService.class,
        AktorConfig.class,
        OppfolgingssakService.class,
        OppfolgingsstatusService.class
})
public class ServiceConfig {

    @Bean
    public BrukereMedOppdateringService brukereMedOppdateringService(OppfolgingsbrukerEndringTemplate oppfolgingsbrukerEndringTemplate, AktoerRegisterService aktoerRegisterService) {
        return new BrukereMedOppdateringService(oppfolgingsbrukerEndringTemplate, aktoerRegisterService);
    }

    @Bean
    public AuthService authService(AktoerRegisterService aktoerRegisterService, VeilarbAbacPepClient pepClient) {
        return new AuthService(aktoerRegisterService, pepClient);
    }

    @Bean
    public ArenaOrdsService arenaOrdsService() {
        return new ArenaOrdsService(new ArenaOrdsTokenProvider());
    }
}
