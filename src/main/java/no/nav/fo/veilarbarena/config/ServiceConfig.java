package no.nav.fo.veilarbarena.config;

import no.nav.fo.veilarbarena.client.AktoerRegisterClient;
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
        AktoerRegisterClient.class,
})
public class ServiceConfig {

    @Bean
    public BrukereMedOppdateringService brukereMedOppdateringService(OppfolgingsbrukerEndringTemplate oppfolgingsbrukerEndringTemplate, AktoerRegisterClient aktoerRegisterClient) {
        return new BrukereMedOppdateringService(oppfolgingsbrukerEndringTemplate, aktoerRegisterClient);
    }
}