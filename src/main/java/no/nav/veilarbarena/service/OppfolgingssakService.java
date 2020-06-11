package no.nav.veilarbarena.service;

import no.nav.veilarbarena.client.ArenaOrdsClient;
import no.nav.veilarbarena.domain.api.OppfolgingssakDTO;
import no.nav.veilarbarena.service.dto.ArenaOppfolgingssakDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OppfolgingssakService {

    private ArenaOrdsClient arenaOrdsClient;

    @Autowired
    public OppfolgingssakService(ArenaOrdsClient arenaOrdsClient) {
        this.arenaOrdsClient = arenaOrdsClient;
    }

    public OppfolgingssakDTO hentOppfolginssak(String fnr) {
        return Optional.ofNullable(arenaOrdsClient.get("oppfoelgingssak", fnr, ArenaOppfolgingssakDTO.class))
                .map(ArenaOppfolgingssakDTO::toOppfolgingssakDTO)
                .orElse(null);
    }
}
