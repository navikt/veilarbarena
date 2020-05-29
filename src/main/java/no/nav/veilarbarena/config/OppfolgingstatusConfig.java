package no.nav.veilarbarena.config;

import no.nav.common.cxf.CXFClient;
import no.nav.common.health.HealthCheck;
import no.nav.common.health.HealthCheckResult;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v2.binding.OppfoelgingsstatusV2;

import no.nav.veilarbarena.controller.SoapOppfolgingstatusController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.common.utils.EnvironmentUtils.getRequiredProperty;

@Configuration
public class OppfolgingstatusConfig {
    public static final String ENDPOINTURL = "VIRKSOMHET_OPPFOELGINGSSTATUS_V2_ENDPOINTURL";

    private static CXFClient<OppfoelgingsstatusV2> oppfoelgingsstatusV2Factory() {
        return new CXFClient<>(OppfoelgingsstatusV2.class)
                .address(getRequiredProperty(ENDPOINTURL))
                .withMetrics();
    }

    @Bean
    public OppfoelgingsstatusV2 oppfoelgingsstatusV2() {
        return oppfoelgingsstatusV2Factory()
                .configureStsForOnBehalfOfWithJWT()
                .build();
    }

    @Bean
    public HealthCheck oppfoelgingsstatusV2HealthCheck() {
        return () -> {
            OppfoelgingsstatusV2 service = oppfoelgingsstatusV2Factory()
                    .configureStsForSystemUser()
                    .build();

            try {
                service.ping();
                return HealthCheckResult.healthy();
            } catch (Exception e) {
                return HealthCheckResult.unhealthy("Klarte ikke å pinge oppfolgingstatus_v2", e);
            }
        };
    }
}
