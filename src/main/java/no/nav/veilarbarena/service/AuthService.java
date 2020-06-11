package no.nav.veilarbarena.service;

import no.nav.common.abac.Pep;
import no.nav.common.abac.domain.AbacPersonId;
import no.nav.common.abac.domain.request.ActionId;
import no.nav.common.auth.subject.SsoToken;
import no.nav.common.auth.subject.SubjectHandler;
import no.nav.common.client.aktorregister.AktorregisterClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AktorregisterClient aktorregisterClient;

    private final Pep veilarbPep;

    @Autowired
    public AuthService(AktorregisterClient aktorregisterClient, Pep veilarbPep) {
        this.aktorregisterClient = aktorregisterClient;
        this.veilarbPep = veilarbPep;
    }

    public void sjekkTilgang(String fnr) {
        String aktorId = aktorregisterClient.hentAktorId(fnr);
        String innloggetBrukerToken = SubjectHandler.getSsoToken()
                .map(SsoToken::getToken)
                .orElseThrow(() -> new IllegalStateException("Fant ikke token til innlogget bruker"));

        if (!veilarbPep.harTilgangTilPerson(innloggetBrukerToken, ActionId.READ, AbacPersonId.aktorId(aktorId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

}
