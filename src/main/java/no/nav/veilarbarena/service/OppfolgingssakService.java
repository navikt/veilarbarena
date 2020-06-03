package no.nav.veilarbarena.service;

import no.nav.veilarbarena.client.ArenaOrdsClient;
import no.nav.veilarbarena.domain.api.OppfolgingssakDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OppfolgingssakService {

    private ArenaOrdsClient arenaOrdsClient;

    @Autowired
    public OppfolgingssakService(ArenaOrdsClient arenaOrdsClient) {
        this.arenaOrdsClient = arenaOrdsClient;
    }

    public OppfolgingssakDTO hentOppfolginssak(String fnr) {
        return arenaOrdsClient.get("oppfoelgingssak", fnr, OppfolgingssakDTO.class);
    }

}
