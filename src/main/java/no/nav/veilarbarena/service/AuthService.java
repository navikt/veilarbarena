package no.nav.veilarbarena.service;

import lombok.SneakyThrows;
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

import java.util.Arrays;

@Slf4j
@Service
public class AuthService {

    private final AuthContextHolder authContextHolder;

    private final Pep veilarbPep;

    @Autowired
    public AuthService(AuthContextHolder authContextHolder,  Pep veilarbPep) {
        this.authContextHolder = authContextHolder;
        this.veilarbPep = veilarbPep;
    }

    public void sjekkTilgang(Fnr fnr) {
        String innloggetBrukerToken = authContextHolder.requireIdTokenString();

        if (!veilarbPep.harTilgangTilPerson(innloggetBrukerToken, ActionId.READ, fnr)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    public boolean erSystembruker() {
        return authContextHolder.erSystemBruker();
    }

    @SneakyThrows
    public void sjekkAtSystembrukerErWhitelistet(String... clientIdWhitelist) {
        String requestingAppClientId = authContextHolder.requireIdTokenClaims().getStringClaim("azp");
        boolean isWhitelisted = Arrays.asList(clientIdWhitelist).contains(requestingAppClientId);

        if (!isWhitelisted) {
            log.error("Systembruker {} er ikke whitelistet", requestingAppClientId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

}
