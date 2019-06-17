package no.nav.fo.veilarbarena.service;

import no.nav.fo.veilarbarena.api.OppfolgingssakDTO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class OppfolgingssakService {

    private ArenaOrdsService arenaOrdsService;

    @Inject
    public OppfolgingssakService(ArenaOrdsService arenaOrdsService) {
        this.arenaOrdsService = arenaOrdsService;
    }

    public OppfolgingssakDTO hentOppfolginssak(String fnr) {
        return arenaOrdsService
                .get("oppfoelgingssak", fnr, OppfolgingssakDTO.class);
    }
}
