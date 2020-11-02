package no.nav.veilarbarena.service;

import no.nav.veilarbarena.client.ArenaOrdsClient;
import no.nav.veilarbarena.domain.api.OppfolgingssakDTO;
import no.nav.veilarbarena.service.dto.ArenaOppfolgingssakDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OppfolgingssakService {

    private ArenaOrdsClient arenaOrdsClient;

    @Autowired
    public OppfolgingssakService(ArenaOrdsClient arenaOrdsClient) {
        this.arenaOrdsClient = arenaOrdsClient;
    }

    public OppfolgingssakDTO hentOppfolginssak(String fnr) {
        return arenaOrdsClient.get("oppfoelgingssak", fnr, ArenaOppfolgingssakDTO.class)
                .map(ArenaOppfolgingssakDTO::toOppfolgingssakDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
