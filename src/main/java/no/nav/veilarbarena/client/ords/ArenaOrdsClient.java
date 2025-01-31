package no.nav.veilarbarena.client.ords;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.client.ords.dto.*;

import java.util.Optional;

public interface ArenaOrdsClient extends HealthCheck {

    Optional<ArenaOppfolgingsstatusDTO> hentArenaOppfolgingsstatus(Fnr fnr);

    Optional<ArenaOppfolgingssakDTO> hentArenaOppfolgingssak(Fnr fnr);

    Optional<ArenaAktiviteterDTO> hentArenaAktiviteter(Fnr fnr);

    Optional<RegistrerIkkeArbeidssokerDto> registrerIkkeArbeidssoker(Fnr fnr);

}
