package no.nav.fo.veilarbarena.config;

import no.nav.apiapp.security.PepClient;
import no.nav.dialogarena.aktor.AktorConfig;
import no.nav.dialogarena.aktor.AktorService;
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
        OppfolgingsstatusService.class,
        UnleashConfig.class,
        AktorConfig.class
})
public class ServiceConfig {

    @Bean
    public BrukereMedOppdateringService brukereMedOppdateringService(OppfolgingsbrukerEndringTemplate oppfolgingsbrukerEndringTemplate, AktoerRegisterService aktoerRegisterService) {
        return new BrukereMedOppdateringService(oppfolgingsbrukerEndringTemplate, aktoerRegisterService);
    }

    @Bean
    public AuthService authService(AktorService aktorService, PepClient pepClient) {
        return new AuthService(aktorService, pepClient);
    }

    @Bean
    public ArenaOrdsService arenaOrdsService() {
        return new ArenaOrdsService(new ArenaOrdsTokenProvider());
    }
}
