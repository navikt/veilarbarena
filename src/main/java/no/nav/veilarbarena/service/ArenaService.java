package no.nav.veilarbarena.service;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.client.ords.ArenaOrdsClient;
import no.nav.veilarbarena.controller.response.ArenaStatusDTO;
import no.nav.veilarbarena.repository.OppfolgingsbrukerRepository;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerEntity;
import no.nav.veilarbarena.service.dto.ArenaOppfolgingssakDTO;
import no.nav.veilarbarena.service.dto.ArenaOppfolgingsstatusDTO;
import no.nav.veilarbarena.utils.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ArenaService {

    private final ArenaOrdsClient arenaOrdsClient;

    private final OppfolgingsbrukerRepository oppfolgingsbrukerRepository;

    @Autowired
    public ArenaService(ArenaOrdsClient arenaOrdsClient, OppfolgingsbrukerRepository oppfolgingsbrukerRepository) {
        this.arenaOrdsClient = arenaOrdsClient;
        this.oppfolgingsbrukerRepository = oppfolgingsbrukerRepository;
    }

    /**
     * Sjekker først i lokal database om bruker ligger der, hvis ikke så går vi direkte mot Arena for å hente status.
     * @param fnr fødselsnummer/dnr til bruker
     * @return status fra lokal database eller direkte fra Arena
     */
    public Optional<ArenaStatusDTO> hentArenaStatus(Fnr fnr) {
        var maybeDbLinkArenaStatus = oppfolgingsbrukerRepository.hentOppfolgingsbruker(fnr.get())
                .map(DtoMapper::mapTilArenaStatusDTO);

        if (maybeDbLinkArenaStatus.isPresent()) {
            return maybeDbLinkArenaStatus;
        }

        return hentArenaOppfolgingsstatus(fnr)
                .map(DtoMapper::mapTilArenaStatusDTO);
    }

    public Boolean hentKanEnkeltReaktiveres(Fnr fnr) {
        return hentArenaOppfolgingsstatus(fnr)
                .map(ArenaOppfolgingsstatusDTO::getKanEnkeltReaktiveres)
                .orElse(null);
    }

    public Optional<OppfolgingsbrukerEntity> hentOppfolgingsbruker(Fnr fnr) {
        return oppfolgingsbrukerRepository.hentOppfolgingsbruker(fnr.get());
    }

    public Optional<ArenaOppfolgingsstatusDTO> hentArenaOppfolgingsstatus(Fnr fnr) {
        return arenaOrdsClient.get("oppfoelgingsstatus", fnr.get(), ArenaOppfolgingsstatusDTO.class);
    }

    public Optional<ArenaOppfolgingssakDTO> hentArenaOppfolginssak(Fnr fnr) {
        return arenaOrdsClient.get("oppfoelgingssak", fnr.get(), ArenaOppfolgingssakDTO.class);
    }

}
