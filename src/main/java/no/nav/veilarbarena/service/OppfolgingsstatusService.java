package no.nav.veilarbarena.service;

import no.nav.veilarbarena.client.ArenaOrdsClient;
import no.nav.veilarbarena.domain.api.OppfolgingsstatusDTO;
import no.nav.veilarbarena.service.dto.ArenaOppfolgingsstatusDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OppfolgingsstatusService {

    private final ArenaOrdsClient arenaOrdsClient;

    @Autowired
    public OppfolgingsstatusService(ArenaOrdsClient arenaOrdsClient) {
        this.arenaOrdsClient = arenaOrdsClient;
    }

    public OppfolgingsstatusDTO hentOppfolgingsstatus(String fnr) {
        return Optional.ofNullable(arenaOrdsClient.get("oppfoelgingsstatus", fnr, ArenaOppfolgingsstatusDTO.class))
                .map(ArenaOppfolgingsstatusDTO::toOppfolgingsstatusDTO)
                .orElse(null);
    }
}
