package no.nav.fo.veilarbarena.service;

import no.nav.apiapp.security.veilarbabac.Bruker;
import no.nav.apiapp.security.veilarbabac.VeilarbAbacPepClient;
import no.nav.dialogarena.aktor.AktorService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class AuthService {

    private final AktorService aktorService;
    private final VeilarbAbacPepClient pepClient;

    @Inject
    public AuthService(AktorService aktorService, VeilarbAbacPepClient pepClient) {
        this.aktorService = aktorService;
        this.pepClient = pepClient;
    }

    public void sjekkTilgang(String fnr) {
        String aktorId = getAktorIdOrThrow(fnr);
        Bruker bruker = Bruker.fraAktoerId(aktorId).medFoedselsnummer(fnr);
        pepClient.sjekkLesetilgangTilBruker(bruker);
    }

    private String getAktorIdOrThrow(String fnr) {
        return aktorService.getAktorId(fnr)
                .orElseThrow(() -> new IllegalArgumentException("Fant ikke aktør for fnr"));
    }
}
