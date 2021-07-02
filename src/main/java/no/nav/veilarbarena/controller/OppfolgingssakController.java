package no.nav.veilarbarena.controller;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.controller.response.OppfolgingssakDTO;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import no.nav.veilarbarena.utils.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/oppfolgingssak")
public class OppfolgingssakController {

    private final AuthService authService;

    private final ArenaService arenaService;

    @Autowired
    public OppfolgingssakController(AuthService authService, ArenaService arenaService) {
        this.authService = authService;
        this.arenaService = arenaService;
    }

    @Deprecated
    @GetMapping("/{fnr}")
    public OppfolgingssakDTO oppfolgingssak(@PathVariable("fnr") Fnr fnr) {
        authService.sjekkTilgang(fnr);

        return arenaService.hentArenaOppfolginssak(fnr)
                .map(DtoMapper::mapTilOppfolgingssakDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
