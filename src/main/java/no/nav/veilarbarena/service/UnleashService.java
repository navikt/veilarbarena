package no.nav.veilarbarena.service;

import lombok.RequiredArgsConstructor;
import no.nav.common.featuretoggle.UnleashClient;
import org.springframework.stereotype.Service;

@Service
public class UnleashService {

    private final UnleashClient unleashClient;

    private static final String UNLEASH_POAO_TILGANG_ENABLED = "veilarbveileder.poao-tilgang-enabled";

    public UnleashService(UnleashClient unleashClient) {
        this.unleashClient = unleashClient;
    }
}

