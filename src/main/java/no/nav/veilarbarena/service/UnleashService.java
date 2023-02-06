package no.nav.veilarbarena.service;

import no.nav.common.featuretoggle.UnleashClient;
import org.springframework.stereotype.Service;

@Service
public class UnleashService {

    private final UnleashClient unleashClient;

    public UnleashService(UnleashClient unleashClient) {
        this.unleashClient = unleashClient;
    }
}

