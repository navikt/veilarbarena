package no.nav.veilarbarena.service;

import no.nav.veilarbarena.domain.api.OppfolgingsstatusDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OppfolgingsstatusService {

    private ArenaOrdsService arenaOrdsService;

    @Autowired
    public OppfolgingsstatusService(ArenaOrdsService arenaOrdsService) {
        this.arenaOrdsService = arenaOrdsService;
    }

    public OppfolgingsstatusDTO hentOppfolgingsstatus(String fnr) {
        return arenaOrdsService.get("oppfoelgingsstatus", fnr, OppfolgingsstatusDTO.class);
    }
}
