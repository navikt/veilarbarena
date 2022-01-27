package no.nav.veilarbarena.controller;

import lombok.RequiredArgsConstructor;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.config.EnvironmentProperties;
import no.nav.veilarbarena.controller.response.ArenaStatusDTO;
import no.nav.veilarbarena.controller.response.KanEnkeltReaktiveresDTO;
import no.nav.veilarbarena.controller.response.OppfolgingssakDTO;
import no.nav.veilarbarena.controller.response.YtelserDTO;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import no.nav.veilarbarena.utils.DtoMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static no.nav.veilarbarena.utils.DtoMapper.mapTilYtelserDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/arena")
public class ArenaController {

    private static final int MANEDER_BAK_I_TID = 2;

    private static final int MANEDER_FREM_I_TID = 1;

    private final AuthService authService;

    private final ArenaService arenaService;

    private final EnvironmentProperties environmentProperties;

    @GetMapping("/status")
    public ArenaStatusDTO hentStatus(@RequestParam("fnr") Fnr fnr) {
        if (!authService.erSystembruker()) {
            authService.sjekkTilgang(fnr);
        } else {
            authService.sjekkAtSystembrukerErWhitelistet(
                    environmentProperties.getPoaoGcpProxyClientId(),
                    environmentProperties.getAmtTiltakClientId(),
                    environmentProperties.getTiltaksgjennomforingApiClientId()
            );
        }

        return arenaService.hentArenaStatus(fnr)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/kan-enkelt-reaktiveres")
    public KanEnkeltReaktiveresDTO hentKanEnkeltReaktiveres(@RequestParam("fnr") Fnr fnr) {
        authService.sjekkTilgang(fnr);

        Boolean kanEnkeltReaktivers = arenaService.hentKanEnkeltReaktiveres(fnr);

        return new KanEnkeltReaktiveresDTO(kanEnkeltReaktivers);
    }

    @GetMapping("/oppfolgingssak")
    public OppfolgingssakDTO hentOppfolgingssak(@RequestParam("fnr") Fnr fnr) {
        authService.sjekkTilgang(fnr);

        return arenaService.hentArenaOppfolginssak(fnr)
                .map(DtoMapper::mapTilOppfolgingssakDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/ytelser")
    public YtelserDTO hentYtelser(
            @RequestParam("fnr") Fnr fnr,
            @RequestParam(value = "fra", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fra,
            @RequestParam(value = "til", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate til
    ) {
        authService.sjekkTilgang(fnr);

        if (fra != null ^ til != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Både \"fra\" og \"til\" må settes eller ingen av de");
        } else if (fra == null) {
            // Hvis "fra" == null så vil alltid "til" være null
            fra = LocalDate.now().minusMonths(MANEDER_BAK_I_TID);
            til = LocalDate.now().plusMonths(MANEDER_FREM_I_TID);
        }

        var ytelseskontrakt = arenaService.hentYtelseskontrakt(fnr, fra, til);

        return mapTilYtelserDTO(ytelseskontrakt);
    }

}
