package no.nav.veilarbarena.service;

import no.nav.veilarbarena.domain.api.OppfolgingssakDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OppfolgingssakService {

    private ArenaOrdsService arenaOrdsService;

    @Autowired
    public OppfolgingssakService(ArenaOrdsService arenaOrdsService) {
        this.arenaOrdsService = arenaOrdsService;
    }

    public OppfolgingssakDTO hentOppfolginssak(String fnr) {
        return arenaOrdsService.get("oppfoelgingssak", fnr, OppfolgingssakDTO.class);
    }

}
