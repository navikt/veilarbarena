package no.nav.fo.veilarbarena.service;

import no.nav.apiapp.security.veilarbabac.Bruker;
import no.nav.apiapp.security.veilarbabac.VeilarbAbacPepClient;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class AuthService {

    private final AktoerRegisterService aktoerRegisterService;
    private final VeilarbAbacPepClient pepClient;

    @Inject
    public AuthService(AktoerRegisterService aktoerRegisterService, VeilarbAbacPepClient pepClient) {
        this.aktoerRegisterService = aktoerRegisterService;
        this.pepClient = pepClient;
    }

    public void sjekkTilgang(String fnr) {
            String aktorId = getAktorIdOrThrow(fnr);
            Bruker bruker = Bruker.fraAktoerId(aktorId).medFoedselsnummer(fnr);
            pepClient.sjekkLesetilgangTilBruker(bruker);
    }

    private String getAktorIdOrThrow(String fnr) {
        return aktoerRegisterService.tilAktorId(fnr);
    }
}
