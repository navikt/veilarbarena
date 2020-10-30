package no.nav.veilarbarena.service;

import no.nav.common.abac.Pep;
import no.nav.common.abac.domain.request.ActionId;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
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
        AktorId aktorId = aktorregisterClient.hentAktorId(Fnr.of(fnr));
        String innloggetBrukerToken = AuthContextHolder.requireIdTokenString();

        if (!veilarbPep.harTilgangTilPerson(innloggetBrukerToken, ActionId.READ, aktorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

}
