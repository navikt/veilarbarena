package no.nav.veilarbarena.controller.v2;

import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbarena.client.ords.dto.PersonRequest;
import no.nav.veilarbarena.controller.response.OppfolgingsbrukerDTO;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@Slf4j
@RestController
@RequestMapping("/api/v2")
public class OppfolgingsbrukerV2Controller {

    private final ArenaService arenaService;

    private final AuthService authService;

    @Autowired
    public OppfolgingsbrukerV2Controller(ArenaService arenaService, AuthService authService) {
        this.arenaService = arenaService;
        this.authService = authService;
    }

    @PostMapping("/hent-oppfolgingsbruker")
    public OppfolgingsbrukerDTO getOppfolgingsbrukerV2(@RequestBody PersonRequest personRequest) {
        authService.sjekkTilgang(personRequest.getFnr());

        return arenaService.hentOppfolgingsbruker(personRequest.getFnr())
                .map(OppfolgingsbrukerDTO::fraOppfolgingsbruker)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/oppfolgingsbruker/hent-personId")
    public String getPersonIdForOppfolgingsbrukerV2(@RequestBody PersonRequest personRequest) {
        authService.sjekkTilgang(personRequest.getFnr());

        return arenaService.hentOppfolgingsbrukerSinPersonId(personRequest.getFnr())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NO_CONTENT));
    }
}
