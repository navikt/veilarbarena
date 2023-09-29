package no.nav.veilarbarena.controller.v2;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.controller.response.OppfolgingsbrukerDTO;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static no.nav.veilarbarena.utils.FnrMaker.hentFnr;


@Slf4j
@RestController
@RequestMapping("/api/v3/oppfolgingsbruker")
public class OppfolgingsbrukerV2Controller {

    private final ArenaService arenaService;

    private final AuthService authService;

    @Autowired
    public OppfolgingsbrukerV2Controller(ArenaService arenaService, AuthService authService) {
        this.arenaService = arenaService;
        this.authService = authService;
    }

    @PostMapping("/")
    public OppfolgingsbrukerDTO getOppfolgingsbrukerV2(@RequestBody String fnr) {
        Fnr fodselsnummer = hentFnr(fnr);
        authService.sjekkTilgang(fodselsnummer);

        return arenaService.hentOppfolgingsbruker(fodselsnummer)
                .map(OppfolgingsbrukerDTO::fraOppfolgingsbruker)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/hentPersonId")
    public String getPersonIdForOppfolgingsbrukerV2(@RequestBody String fnr) {
        Fnr fodselsnummer = hentFnr(fnr);
        authService.sjekkTilgang(fodselsnummer);

        return arenaService.hentOppfolgingsbrukerSinPersonId(fodselsnummer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NO_CONTENT));
    }
}
