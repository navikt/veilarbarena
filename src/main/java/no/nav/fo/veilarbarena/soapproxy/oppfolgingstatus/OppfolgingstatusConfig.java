package no.nav.fo.veilarbarena.soapproxy.oppfolgingstatus;

import no.nav.fo.veilarbarena.utils.ServiceUtils;
import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v1.binding.OppfoelgingsstatusV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Configuration
public class OppfolgingstatusConfig {
    public static final String ENDPOINTURL = "VIRKSOMHET_OPPFOELGINGSSTATUS_V1_ENDPOINTURL";

    private static CXFClient<OppfoelgingsstatusV1> oppfoelgingsstatusV1Factory() {
        return new CXFClient<>(OppfoelgingsstatusV1.class)
                .address(getRequiredProperty(ENDPOINTURL))
                .withMetrics();
    }

    @Bean
    public OppfolgingstatusController oppfolgingstatusController() {
        return new OppfolgingstatusController();
    }

    @Bean
    public OppfolgingstatusService oppfolgingstatusService(OppfoelgingsstatusV1 service) {
        return new OppfolgingstatusService(service);
    }

    @Bean
    public OppfoelgingsstatusV1 oppfoelgingsstatusV1() {
        return oppfoelgingsstatusV1Factory()
                .configureStsForOnBehalfOfWithJWT()
                .build();
    }

    @Bean
    public Pingable oppfoelgingsstatusV1Ping() {
        OppfoelgingsstatusV1 service = oppfoelgingsstatusV1Factory()
                .configureStsForSystemUser()
                .build();
        Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(UUID.randomUUID().toString(),
                "OPPFOELGINGSTATUS_V1 via " + getRequiredProperty(ENDPOINTURL),
                "Ping av oppfolgingstatus_v1. Henter informasjon om oppfølgingsstatus fra arena.",
                true
        );

        return ServiceUtils.createPingable(service::ping, metadata);
    }
}
