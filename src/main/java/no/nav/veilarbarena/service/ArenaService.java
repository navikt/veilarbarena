package no.nav.veilarbarena.service;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.client.ords.ArenaOrdsClient;
import no.nav.veilarbarena.client.ords.dto.ArenaAktiviteterDTO;
import no.nav.veilarbarena.client.ords.dto.ArenaOppfolgingssakDTO;
import no.nav.veilarbarena.client.ords.dto.ArenaOppfolgingsstatusDTO;
import no.nav.veilarbarena.client.ords.dto.RegistrerIkkeArbeidssokerDto;
import no.nav.veilarbarena.controller.response.ArenaStatusDTO;
import no.nav.veilarbarena.repository.OppfolgingsbrukerRepository;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerEntity;
import no.nav.veilarbarena.utils.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ArenaService {

    private final ArenaOrdsClient arenaOrdsClient;

    private final OppfolgingsbrukerRepository oppfolgingsbrukerRepository;

    @Autowired
    public ArenaService(
            ArenaOrdsClient arenaOrdsClient,
            OppfolgingsbrukerRepository oppfolgingsbrukerRepository
    ) {
        this.arenaOrdsClient = arenaOrdsClient;
        this.oppfolgingsbrukerRepository = oppfolgingsbrukerRepository;
    }

    /**
     * Sjekker først i lokal database om bruker ligger der, hvis ikke så går vi direkte mot Arena for å hente status.
     *
     * @param fnr       fødselsnummer/dnr til bruker
     * @param forceSync Ikke bruk lokal database, men hent status direkte fra arena
     * @return status fra lokal database eller direkte fra Arena
     */
    public Optional<ArenaStatusDTO> hentArenaStatus(Fnr fnr, boolean forceSync) {
        if (forceSync) {
            return hentArenaOppfolgingsstatus(fnr)
                    .map(DtoMapper::mapTilArenaStatusDTO);
        } else {
            var maybeDbLinkArenaStatus = oppfolgingsbrukerRepository.hentOppfolgingsbruker(fnr.get())
                    .map(DtoMapper::mapTilArenaStatusDTO);

            if (maybeDbLinkArenaStatus.isPresent()) {
                return maybeDbLinkArenaStatus;
            }

            return hentArenaOppfolgingsstatus(fnr)
                    .map(DtoMapper::mapTilArenaStatusDTO);
        }
    }

    public Boolean hentKanEnkeltReaktiveres(Fnr fnr) {
        return hentArenaOppfolgingsstatus(fnr)
                .map(ArenaOppfolgingsstatusDTO::getKanEnkeltReaktiveres)
                .orElse(null);
    }

    public Optional<OppfolgingsbrukerEntity> hentOppfolgingsbruker(Fnr fnr) {
        return oppfolgingsbrukerRepository.hentOppfolgingsbruker(fnr.get());
    }

    public Optional<String> hentOppfolgingsbrukerSinPersonId(Fnr fnr) {
        return oppfolgingsbrukerRepository.hentOppfolgingsbrukerSinPersonId(fnr.get());
    }

    public void refreshMaterializedOppfolgingsBrukerView() {
        oppfolgingsbrukerRepository.refreshMaterializedOppfolgingsBrukerView();
    }

    public Optional<ArenaOppfolgingsstatusDTO> hentArenaOppfolgingsstatus(Fnr fnr) {
        return arenaOrdsClient.hentArenaOppfolgingsstatus(fnr);
    }

    public Optional<ArenaOppfolgingssakDTO> hentArenaOppfolginssak(Fnr fnr) {
        return arenaOrdsClient.hentArenaOppfolgingssak(fnr);
    }

    public Optional<ArenaAktiviteterDTO> hentArenaAktiviteter(Fnr fnr) {
        return arenaOrdsClient.hentArenaAktiviteter(fnr);
    }

    public Optional<RegistrerIkkeArbeidssokerDto> registrerIkkeArbeidssoker(Fnr fnr) {
        return arenaOrdsClient.registrerIkkeArbeidssoker(fnr);
    }

}
