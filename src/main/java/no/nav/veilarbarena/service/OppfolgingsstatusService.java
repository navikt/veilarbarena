package no.nav.veilarbarena.service;

import no.nav.veilarbarena.client.ArenaOrdsClient;
import no.nav.veilarbarena.domain.api.OppfolgingsstatusDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OppfolgingsstatusService {

    private ArenaOrdsClient arenaOrdsClient;

    @Autowired
    public OppfolgingsstatusService(ArenaOrdsClient arenaOrdsClient) {
        this.arenaOrdsClient = arenaOrdsClient;
    }

    public OppfolgingsstatusDTO hentOppfolgingsstatus(String fnr) {
        return arenaOrdsClient.get("oppfoelgingsstatus", fnr, OppfolgingsstatusDTO.class);
    }
}
