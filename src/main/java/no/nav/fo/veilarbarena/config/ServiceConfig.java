package no.nav.fo.veilarbarena.config;

import no.nav.fo.veilarbarena.service.AktoerRegisterService;
import no.nav.apiapp.security.veilarbabac.VeilarbAbacPepClient;
import no.nav.fo.veilarbarena.service.AuthService;
import no.nav.fo.veilarbarena.service.BrukereMedOppdateringService;
import no.nav.fo.veilarbarena.service.OppfolgingsbrukerEndringTemplate;
import no.nav.fo.veilarbarena.soapproxy.oppfolgingstatus.OppfolgingstatusConfig;
import no.nav.sbl.featuretoggle.unleash.UnleashService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;

@Configuration
@Import({
        OppfolgingstatusConfig.class,
        KafkaConfig.class,
        AktoerRegisterService.class,
        UnleashConfig.class
})
public class ServiceConfig {

    @Inject
    private UnleashService unleashService;

    @Bean
    public BrukereMedOppdateringService brukereMedOppdateringService(OppfolgingsbrukerEndringTemplate oppfolgingsbrukerEndringTemplate, AktoerRegisterService aktoerRegisterService) {
        return new BrukereMedOppdateringService(oppfolgingsbrukerEndringTemplate, aktoerRegisterService);
    }

    @Bean
    public AuthService authService(AktoerRegisterService aktoerRegisterService, VeilarbAbacPepClient pepClient, UnleashService unleashService) {
        return new AuthService(aktoerRegisterService, pepClient, unleashService);
    }
}
