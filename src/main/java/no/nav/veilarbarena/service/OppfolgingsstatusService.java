package no.nav.veilarbarena.service;

import no.nav.veilarbarena.client.ords.ArenaOrdsClient;
import no.nav.veilarbarena.controller.response.OppfolgingsstatusDTO;
import no.nav.veilarbarena.service.dto.ArenaOppfolgingsstatusDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OppfolgingsstatusService {

    private final ArenaOrdsClient arenaOrdsClient;

    @Autowired
    public OppfolgingsstatusService(ArenaOrdsClient arenaOrdsClient) {
        this.arenaOrdsClient = arenaOrdsClient;
    }

    public OppfolgingsstatusDTO hentOppfolgingsstatus(String fnr) {
        return arenaOrdsClient.get("oppfoelgingsstatus", fnr, ArenaOppfolgingsstatusDTO.class)
                .map(ArenaOppfolgingsstatusDTO::toOppfolgingsstatusDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
