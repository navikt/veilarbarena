package no.nav.fo.veilarbarena.service;

import no.nav.apiapp.security.PepClient;
import no.nav.dialogarena.aktor.AktorService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class AuthService {

    private final AktorService aktorService;
    private final PepClient pepClient;

    @Inject
    public AuthService(AktorService aktorService,
                       PepClient pepClient) {
        this.aktorService = aktorService;
        this.pepClient = pepClient;
    }

    public void sjekkTilgang(String fnr) {
        String aktorId = getAktorIdOrThrow(fnr);
        pepClient.sjekkLesetilgangTilAktorId(aktorId);
    }

    private String getAktorIdOrThrow(String fnr) {
        return aktorService.getAktorId(fnr).orElseThrow(() -> new IllegalArgumentException("Fant ikke aktør id."));
    }
}
