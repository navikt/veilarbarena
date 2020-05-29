package no.nav.veilarbarena.service;

import no.nav.veilarbarena.domain.api.OppfolgingsstatusDTO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class OppfolgingsstatusService {

    private ArenaOrdsService arenaOrdsService;

    @Inject
    public OppfolgingsstatusService(ArenaOrdsService arenaOrdsService) {
        this.arenaOrdsService = arenaOrdsService;
    }

    public OppfolgingsstatusDTO hentOppfolgingsstatus(String fnr) {
        return arenaOrdsService
                .get("oppfoelgingsstatus", fnr, OppfolgingsstatusDTO.class);
    }
}
