package no.nav.veilarbarena.controller;

import lombok.RequiredArgsConstructor;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.controller.response.ArenaStatusDTO;
import no.nav.veilarbarena.controller.response.KanEnkeltReaktiveresDTO;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/arena")
public class ArenaController {

    private final AuthService authService;

    private final ArenaService arenaService;

    @GetMapping("/status")
    public ArenaStatusDTO hentStatus(@RequestParam("fnr") Fnr fnr) {
        authService.sjekkTilgang(fnr);

        return arenaService.hentArenaStatus(fnr)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/kan-enkelt-reaktiveres")
    public KanEnkeltReaktiveresDTO hentKanEnkeltReaktiveres(@RequestParam("fnr") Fnr fnr) {
        authService.sjekkTilgang(fnr);

        Boolean kanEnkeltReaktivers = arenaService.hentKanEnkeltReaktiveres(fnr);

        return new KanEnkeltReaktiveresDTO(kanEnkeltReaktivers);
    }

}
