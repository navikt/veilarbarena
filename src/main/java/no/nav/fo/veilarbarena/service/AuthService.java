package no.nav.fo.veilarbarena.service;

import no.nav.apiapp.security.veilarbabac.Bruker;
import no.nav.apiapp.security.veilarbabac.VeilarbAbacPepClient;
import no.nav.sbl.featuretoggle.unleash.UnleashService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class AuthService {

    private final AktoerRegisterService aktoerRegisterService;
    private final VeilarbAbacPepClient pepClient;
    private final UnleashService unleashService;

    @Inject
    public AuthService(AktoerRegisterService aktoerRegisterService, VeilarbAbacPepClient pepClient, UnleashService unleashService) {
        this.aktoerRegisterService = aktoerRegisterService;
        this.pepClient = pepClient;
        this.unleashService = unleashService;
    }

    public void sjekkTilgang(String fnr) {
        if (unleashService.isEnabled("veilarbarena.sjekk_tilgang_abac")) {
            String aktorId = getAktorIdOrThrow(fnr);
            Bruker bruker = Bruker.fraAktoerId(aktorId).medFoedselsnummer(fnr);
            pepClient.sjekkLesetilgangTilBruker(bruker);
        }
    }

    private String getAktorIdOrThrow(String fnr) {
        return aktoerRegisterService.tilAktorId(fnr);
    }
}
