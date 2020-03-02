package no.nav.fo.veilarbarena.service;

import no.nav.apiapp.security.PepClient;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class AuthService {

    private final AktoerRegisterService aktoerRegisterService;
    private final PepClient pepClient;

    @Inject
    public AuthService(AktoerRegisterService aktoerRegisterService, PepClient pepClient) {
        this.aktoerRegisterService = aktoerRegisterService;
        this.pepClient = pepClient;
    }

    public void sjekkTilgang(String fnr) {
        String aktorId = getAktorIdOrThrow(fnr);
        pepClient.sjekkLesetilgangTilAktorId(aktorId);
    }

    private String getAktorIdOrThrow(String fnr) {
        return aktoerRegisterService.tilAktorId(fnr);
    }
}
