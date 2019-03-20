package no.nav.fo.veilarbarena.soapproxy.oppfolgingstatus;

import no.nav.fo.veilarbarena.utils.ServiceUtils;
import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v2.binding.OppfoelgingsstatusV2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Configuration
public class OppfolgingstatusConfig {
    public static final String ENDPOINTURL = "VIRKSOMHET_OPPFOELGINGSSTATUS_V2_ENDPOINTURL";

    private static CXFClient<OppfoelgingsstatusV2> oppfoelgingsstatusV2Factory() {
        return new CXFClient<>(OppfoelgingsstatusV2.class)
                .address(getRequiredProperty(ENDPOINTURL))
                .withMetrics();
    }

    @Bean
    public OppfolgingstatusController oppfolgingstatusController() {
        return new OppfolgingstatusController();
    }

    @Bean
    public OppfolgingstatusService oppfolgingstatusService(OppfoelgingsstatusV2 service) {
        return new OppfolgingstatusService(service);
    }

    @Bean
    public OppfoelgingsstatusV2 oppfoelgingsstatusV2() {
        return oppfoelgingsstatusV2Factory()
                .configureStsForOnBehalfOfWithJWT()
                .build();
    }

    @Bean
    public Pingable oppfoelgingsstatusV2Ping() {
        OppfoelgingsstatusV2 service = oppfoelgingsstatusV2Factory()
                .configureStsForSystemUser()
                .build();
        Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(UUID.randomUUID().toString(),
                "OPPFOELGINGSTATUS_V2 via " + getRequiredProperty(ENDPOINTURL),
                "Ping av oppfolgingstatus_v2. Henter informasjon om oppfølgingsstatus fra arena.",
                true
        );

        return ServiceUtils.createPingable(service::ping, metadata);
    }
}
