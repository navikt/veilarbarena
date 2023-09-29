package no.nav.veilarbarena.controller.v2;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.controller.response.OppfolgingssakDTO;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import no.nav.veilarbarena.utils.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static no.nav.veilarbarena.utils.FnrMaker.hentFnr;

@RestController
@RequestMapping("/api/oppfolgingssak")
public class OppfolgingssakV2Controller {

    private final AuthService authService;

    private final ArenaService arenaService;

    @Autowired
    public OppfolgingssakV2Controller(AuthService authService, ArenaService arenaService) {
        this.authService = authService;
        this.arenaService = arenaService;
    }

    @PostMapping("/")
    public OppfolgingssakDTO oppfolgingssakV2(@RequestBody String fnr) {
        Fnr fodselsnummer = hentFnr(fnr);
        authService.sjekkTilgang(fodselsnummer);

        return arenaService.hentArenaOppfolginssak(fodselsnummer)
                .map(DtoMapper::mapTilOppfolgingssakDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}