package no.nav.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.abac.Pep;
import no.nav.common.abac.domain.request.ActionId;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class AuthService {

    private final AuthContextHolder authContextHolder;

    private final AktorOppslagClient aktorOppslagClient;

    private final Pep veilarbPep;

    @Autowired
    public AuthService(AuthContextHolder authContextHolder, AktorOppslagClient aktorOppslagClient, Pep veilarbPep) {
        this.authContextHolder = authContextHolder;
        this.aktorOppslagClient = aktorOppslagClient;
        this.veilarbPep = veilarbPep;
    }

    public void sjekkTilgang(Fnr fnr) {
        AktorId aktorId = aktorOppslagClient.hentAktorId(fnr);
        String innloggetBrukerToken = authContextHolder.requireIdTokenString();

        if (!veilarbPep.harTilgangTilPerson(innloggetBrukerToken, ActionId.READ, aktorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    public boolean erSystembruker() {
        return authContextHolder.erSystemBruker();
    }

}
