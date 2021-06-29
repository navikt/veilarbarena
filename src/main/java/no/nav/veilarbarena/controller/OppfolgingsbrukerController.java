package no.nav.veilarbarena.controller;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.controller.response.OppfolgingsbrukerDTO;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/oppfolgingsbruker")
public class OppfolgingsbrukerController {

    private final ArenaService arenaService;

    private final AuthService authService;

    @Autowired
    public OppfolgingsbrukerController(ArenaService arenaService, AuthService authService) {
        this.arenaService = arenaService;
        this.authService = authService;
    }

    @Deprecated
    @GetMapping("/{fnr}")
    public OppfolgingsbrukerDTO getOppfolgingsbruker(@PathVariable("fnr") Fnr fnr) {
        authService.sjekkTilgang(fnr);

        return arenaService.hentOppfolgingsbruker(fnr)
                .map(OppfolgingsbrukerDTO::fraOppfolgingsbruker)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

}
