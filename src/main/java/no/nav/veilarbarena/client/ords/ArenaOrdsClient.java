package no.nav.veilarbarena.client.ords;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.client.ords.dto.ArenaAktiviteterDTO;
import no.nav.veilarbarena.client.ords.dto.ArenaOppfolgingssakDTO;
import no.nav.veilarbarena.client.ords.dto.ArenaOppfolgingsstatusDTO;

import java.util.Optional;

public interface ArenaOrdsClient extends HealthCheck {

    Optional<ArenaOppfolgingsstatusDTO> hentArenaOppfolgingsstatus(Fnr fnr);

    Optional<ArenaOppfolgingssakDTO> hentArenaOppfolgingssak(Fnr fnr);

    Optional<ArenaAktiviteterDTO> hentArenaAktiviteter(Fnr fnr);

}
