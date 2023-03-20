package no.nav.veilarbarena.service;

import no.nav.common.featuretoggle.UnleashClient;
import org.springframework.stereotype.Service;

@Service
public class UnleashService {

    private final UnleashClient unleashClient;

	private static final String UNLEASH_POAO_TILGANG_ENABLED = "veilarbarena.poao-tilgang-enabled";

    public UnleashService(UnleashClient unleashClient) {
        this.unleashClient = unleashClient;
    }

	public boolean skalBrukePoaoTilgang() {
		return unleashClient.isEnabled(UNLEASH_POAO_TILGANG_ENABLED);
	}
}

