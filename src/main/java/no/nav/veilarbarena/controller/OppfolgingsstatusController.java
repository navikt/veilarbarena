package no.nav.veilarbarena.controller;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.controller.response.OppfolgingsstatusDTO;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import no.nav.veilarbarena.utils.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static no.nav.veilarbarena.utils.FnrMaker.hentFnr;

@RestController
@RequestMapping("/api/oppfolgingsstatus")
public class OppfolgingsstatusController {

    private final AuthService authService;

    private final ArenaService arenaService;

    @Autowired
    public OppfolgingsstatusController(AuthService authService, ArenaService arenaService) {
        this.authService = authService;
        this.arenaService = arenaService;
    }

    @Deprecated
    @GetMapping("/{fnr}")
    public OppfolgingsstatusDTO hentArenaOppfolgingsstatus(@PathVariable("fnr") Fnr fnr) {
        authService.sjekkTilgang(fnr);

        return arenaService.hentArenaOppfolgingsstatus(fnr)
                .map(DtoMapper::mapTilOppfolgingsstatusDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/")
    public OppfolgingsstatusDTO hentArenaOppfolgingsstatusV2(@RequestBody  String fnr) {
        Fnr fodselsnummer = hentFnr(fnr);
        authService.sjekkTilgang(fodselsnummer);
        return arenaService.hentArenaOppfolgingsstatus(fodselsnummer)
                .map(DtoMapper::mapTilOppfolgingsstatusDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
